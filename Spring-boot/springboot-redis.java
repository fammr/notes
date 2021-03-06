------------------------------------
Redis-整合单机版					|
------------------------------------
	# 使用lettuce
	# 依赖
		<dependency>
	        <groupId>org.springframework.boot</groupId>
	        <artifactId>spring-boot-starter-data-redis</artifactId>
    	</dependency>
		<dependency>
		    <groupId>io.lettuce</groupId>
		    <artifactId>lettuce-core</artifactId>
		</dependency>
		<dependency>
		    <groupId>org.apache.commons</groupId>
		    <artifactId>commons-pool2</artifactId>
		</dependency>
	
	# 配置文件
		# Redis数据库索引（默认为0）
		spring.redis.database=0  
		# Redis服务器地址
		spring.redis.host=192.168.0.58
		# Redis服务器连接端口
		spring.redis.port=6379  
		# Redis服务器连接密码(默认为空)
		spring.redis.password=  
		# 连接池最大连接数(使用负值表示没有限制)
		spring.redis.lettuce.pool.max-active=8  
		# 连接池最大阻塞等待时间(使用负值表示没有限制)
		spring.redis.lettuce.pool.max-wait=-1  
		# 连接池中的最大空闲连接
		spring.redis.lettuce.pool.max-idle=8  
		# 连接池中的最小空闲连接
		spring.redis.lettuce.pool.min-idle=0  
		# 连接超时时间(毫秒)
		spring.redis.timeout=2000

	# 使用
		* StringRedisTemplate
			* 是RedisTemplate的子类
			* 一般大多数都是使用它
		* 针对jedis客户端中大量api进行了归类封装,将同一类型操作封装为operation接口
			ValueOperations	简单K-V操作
			SetOperations	set类型数据操作
			ZSetOperations	zset类型数据操作
			HashOperations	针对map类型的数据操作
			ListOperations	针对list类型的数据操作
		* demo
			@Autowired
			private StringRedisTemplate stringRedisTemplate;

			stringRedisTemplate.opsForValue();		//获取操作简单k-v的api
			stringRedisTemplate.opsForSet();		//获取操作set的api
	
		
			
------------------------------------
Redis-整合集群						|
------------------------------------	
	
	

------------------------------------
Redis- scan 代替 keys *				|
------------------------------------
	public void scan(String pattern, Consumer<byte[]> consumer) {
		this.stringRedisTemplate.execute((RedisConnection connection) -> {
			try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().count(Long.MAX_VALUE).match(pattern).build())) {
				cursor.forEachRemaining(consumer);
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}

------------------------------------
Redis- 过期key的监听				|
------------------------------------
	# redis必须开启配置
		 notify-keyspace-events "Ex"  # 监听key的过期事件

	# configuration的配置
		@Configuration
		public class RedisConfiguration {
		 
			@Autowired 
			private RedisConnectionFactory redisConnectionFactory;
			
			@Bean
			public RedisMessageListenerContainer redisMessageListenerContainer() {
				//创建监听容器
				RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
				//设置连接工厂
				redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
				return redisMessageListenerContainer;
			}
			
			@Bean
			public KeyExpiredListener keyExpiredListener() {
				return new KeyExpiredListener(this.redisMessageListenerContainer());
			}
		}
	
	# KeyExpiredListener 监听器
		import java.nio.charset.StandardCharsets;

		import org.slf4j.Logger;
		import org.slf4j.LoggerFactory;
		import org.springframework.data.redis.connection.Message;
		import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
		import org.springframework.data.redis.listener.RedisMessageListenerContainer;

		public class KeyExpiredListener extends KeyExpirationEventMessageListener {

			private static final Logger LOGGER = LoggerFactory.getLogger(KeyExpiredListener.class);

			public KeyExpiredListener(RedisMessageListenerContainer listenerContainer) {
				super(listenerContainer);
			}

			@Override
			protected void doHandleMessage(Message message) {
				String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
				String key = new String(message.getBody(), StandardCharsets.UTF_8);
				LOGGER.info("redis key 过期：channel={},key={}", channel, key);
			}
		}

------------------------------------
Redis - 自定义Template序列化		|
------------------------------------
	# 自定义 Templete 类,实现 RedisTemplate,泛型为key和Value
	# 实例化该类
		- 设置连接工厂
		- key的序列化类
		- value的序列化列
		- 其他redis数据结构的k/v序列化类

	# 简单的fastjson序列化
		public class JsonRedisTemplate extends RedisTemplate<String, JSONObject> {
		}

		@Bean
		public JsonRedisTemplate jsonRedisTemplate() {
			JsonRedisTemplate jsonRedisTemplate = new JsonRedisTemplate();
			jsonRedisTemplate.setConnectionFactory(this.redisConnectionFactory);
			jsonRedisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
			jsonRedisTemplate.setValueSerializer(new RedisSerializer<JSONObject>() {
				@Override
				public byte[] serialize(JSONObject t) throws SerializationException {
					return t.toJSONString().getBytes(StandardCharsets.UTF_8);
				}
				@Override
				public JSONObject deserialize(byte[] bytes) throws SerializationException {
					if(bytes == null) {
						return null;
					}
					return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
				}
			});
			// jsonRedisTemplate.setHashKeySerializer(hashKeySerializer);
			// jsonRedisTemplate.setHashValueSerializer();
			return jsonRedisTemplate;
		}