package org.pce.core
/**
 * 流程编排引擎
 */
class ChoreographyEngine {

    /**
     * 初始化配置
     * @param xml xml内容
     * @param pGroovyFilesPath groovy文件路径
     * @param pLibsPath lib包路径
     */
    ChoreographyEngine init(pXml, pGroovyFilesPath, String pLibsPath) {

        //初始化groovy文件
        def groovyFilesPath
        if (pGroovyFilesPath instanceof String)
            groovyFilesPath = new File(pGroovyFilesPath)
        else if (pGroovyFilesPath instanceof File)
            groovyFilesPath = pGroovyFilesPath
        else return null

        this.readGroovyFiles(groovyFilesPath, pLibsPath)


        def xml
        if (pXml instanceof String)
            xml = pXml as String
        else if (pXml instanceof File)
            xml = pXml.getText("UTF-8")
        else return null
        def kmc = new XmlParser().parseText(xml)//解析xml

        //用于存放连接的集合
        List<Connect> connects = new ArrayList<>()
        ConnectHandler connectHandler = new ConnectHandler(connects: connects)

        //初始化节点
        kmc.flow.nodes.node.each { $node ->
            String type = $node.attributes()["type"]
            Class clazz = nodeClasses.get(type)
            Node node = clazz.newInstance() as Node

            //通用属性
            node.id_ = $node.attributes()["id"]
            node.name_ = $node.attributes()["name"]
            node.describe_ = $node.attributes()["describe"]
            node.type_ = type
            node.class_ = clazz.getName()

            //个性化入参
            Map _params = new HashMap()
            $node.properties.each { $property ->
                String name = $property.attributes()["name"]
                String value = $property.text().trim()
                _params.put(name, value)
            }
            node.init(_params)

            node.next = connectHandler//加入next

            //加入节点集合
            nodes << node

            //找到启动器
            //@TODO 这里约定，启动器type name需要以Starter结尾
            if (type.lastIndexOf("Starter") > -1) {
                starters << node//插入启动器集合
            }
        }

        //初始化连接
        kmc.flow.connects.connect.each { $connect ->
            //得到所有属性
            String connectId = $connect.attributes()["id"]
            String connectName = $connect.attributes()["name"]
            String connectForm = $connect.attributes()["from"]
            String connectTo = $connect.attributes()["to"]
            String connectExpression = "true"
            $connect.properties.each { $property ->
                String name = $property.attributes()["name"]
                if (name == "expression")
                    connectExpression = $property.text().trim()
            }

            //得到from和to的对象
            def fromNode = nodes.find { node -> node.id_ == connectForm }
            def toNode = nodes.find { node -> node.id_ == connectTo }

            //插入连接集合
            connects << new Connect(id: connectId, name: connectName, from: fromNode, to: toNode, expression: connectExpression)
        }

        this
    }

    /**
     * 读取groovy文件，并把Node类型的保存下来
     * @param dir groovy文件路径
     * @param pLibsPath lib文件路径
     */
    void readGroovyFiles(File dir, String pLibsPath) {
        GroovyClassLoader loader = new GroovyClassLoader()
        new File(pLibsPath).listFiles().each {
            loader.addClasspath(it.getPath())
        }
        dir.eachFileRecurse {
            if (it.isFile()) {
                def fileName = it.name
                def path = it.getCanonicalPath()

                def names = null
                if (File.separator == "\\")
                    names = path.split(File.separator + File.separator)
                else if (File.separator == "/")
                    names = path.split(File.separator)
                def key = names[names.length - 2]//计算出key（目录名称）

                if (fileName.lastIndexOf(".groovy") > -1) {//如果是groovy文件
                    try {
                        Class clazz = loader.parseClass(it.getText("utf-8"))
                        if (Node.class.isAssignableFrom(clazz)) {//如果是Node类的实现
                            Class<Node> groovyClass = clazz as Class<Node>
                            nodeClasses.put(key, groovyClass)
                        }
                    } catch (Exception e) {
                        println e
                    }
                }
            }
        }
    }

    void refreshGroovyFiles() {
        nodeClasses.clear()
    }

    /**
     * 启动
     */
    void start(Map globalMemory) {
        if (globalMemory == null)
            globalMemory = new HashMap()
        globalMemory.put("finishHandler", new ArrayList<FinishHandler>())

        starters.each { starter ->
            starter.handle(globalMemory)
        }
    }

    /**
     * 停止
     */
    void stop() {
        nodes.each {
            it.shutdown()
        }
        nodes.clear()
    }

    //用于存放多个starter
    private final List<Node> starters = new ArrayList<>()

    //用于存放所有的节点
    private final List<Node> nodes = new ArrayList()

    //用于存放所有节点的class
    private final Map<String, Class<Node>> nodeClasses = new HashMap<>()

}
