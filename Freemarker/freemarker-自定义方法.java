----------------------------
标签实现					|
----------------------------
	# 语法
		<#function name param1 param2 ... paramN>
		  ...
		  <#return returnValue>
		  ...
		</#function>
		
		* name 方法名称
		* 可以定义多个参数
		* paramN,最后一个参数, 可以可选的包含一个尾部省略(...)这就意味着宏接受可变的参数数量,局部变量 paramN 将是额外参数的序列(变成参数)
		* 参数可以有默认值,但是必须排在普通参数的后面
		* return 指令是必须的
		* 方法内的所有输出都会被忽略
	
	# Demo
		<#function foo v1 v2 v3...>
			<#local x = v1 + v2>
			<#list v3 as i>
				<#local x = i + x>
			</#list>
			<#return x>
		</#function>

		${foo(1,2,3,4,5)}		//15

	# 参数类型的转换
		* 参数类型都是:TemplateModel 子类

		* 字符串参数 func('123');
			SimpleScalar simpleScalar = ((SimpleScalar)arguments.get(0));
			simpleScalar.getAsString() //simpleScalar.toString();
		
		* 数值参数 func(123);
			SimpleNumber simpleNumber = ((SimpleNumber)arguments.get(0));
			simpleNumber.getAsNumber().intValue();
			simpleNumber.getAsNumber().longValue();
		
		* 序列参数 func([1,2,3]);
			SimpleSequence simpleSequence = ((SimpleSequence)arguments.get(0));
			TemplateModel value = param.get(0);	//这个value,可能是任意的常量类型(取决于你数组中元素的类型)
			int size = param.size();			//数组长度
			param.add(new Object());			//添加新的元素

		*  Hash常量 func({'v1':'51515','v2':[1,2,3,4]})
			TemplateHashModelEx2 templateHashModelEx2 = (TemplateHashModelEx2)arguments.get(0);
			TemplateModel templateModel = templateHashModelEx2.get("Key");		//获取key
			templateHashModelEx2.size();											//Hash长度
			//迭代
			KeyValuePairIterator keyValuePairIterator = templateHashModelEx2.keyValuePairIterator();
			while(keyValuePairIterator.hasNext()) {
				KeyValuePair keyValuePair = keyValuePairIterator.next();
				TemplateModel key = keyValuePair.getKey();
				TemplateModel value = keyValuePair.getValue();
			}
		
		* Map 变量 
			* 参数是 HashMap

			SimpleHash simpleHash = (SimpleHash)arguments.get(0);
			TemplateModel templateModel = simpleHash.get("Key");		//获取key
			simpleHash.size();											//Hash长度
			simpleHash.containsKey("Key");								//判读key是否存在
			simpleHash.put("age", 23);									//添加元素到map
			simpleHash.isEmpty();
			//迭代
			KeyValuePairIterator keyValuePairIterator = simpleHash.keyValuePairIterator();
			while(keyValuePairIterator.hasNext()) {
				KeyValuePair keyValuePair = keyValuePairIterator.next();
				TemplateModel key = keyValuePair.getKey();
				TemplateModel value = keyValuePair.getValue();
			}
		
		*  对象变量
			* 参数是Java对象
			StringModel stringModel = (StringModel)arguments.get(0);
			TemplateModel templateModel = stringModel.get("");
			TemplateModel templateModel2 = stringModel.getAPI();
			String string = stringModel.getAsString();
			TemplateCollectionModel templateCollectionModel = stringModel.keys();
			int size = stringModel.size();

		*  Date 参数 func(.now);
			SimpleDate simpleDate = (SimpleDate)arguments.get(0);
			Date date = simpleDate.getAsDate();		//获取日期
			int simpleDate.getDateType();			//获取日期的类型

			* dateType 常量
				TemplateDateModel.UNKNOWN;	
				TemplateDateModel.TIME;
				TemplateDateModel.DATE;
				TemplateDateModel.DATETIME;

		
		* 这设计感觉咋那么瓜皮???

----------------------------
编码实现					|
----------------------------
	# 实现接口: TemplateMethodModelEx 
		import java.util.List;

		import freemarker.core.Environment;
		import freemarker.template.TemplateMethodModelEx;
		import freemarker.template.TemplateModelException;

		public class FooFunction implements TemplateMethodModelEx{

			public FooFunction() {
				System.out.println("new");
			}
			
			//arguments 就是传递进来的参数
			@SuppressWarnings("rawtypes")
			@Override
			public Object exec(List arguments) throws TemplateModelException {
				//获取当前运行环境
				Environment environment = Environment.getCurrentEnvironment();
				System.out.println(environment);
				return "result";
			}
		}

	
	# 把该实例给模版引擎,作为一个变量存在
		* configuration.setSharedVariable("fooFunc", new FooFunction());
		* 可以设置为共享变量,以单例形式存在
	
	# 在模板引擎中使用
		${fooFunc("123456")}
	



