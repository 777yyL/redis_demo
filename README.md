# redis_demo

### Redis

非关系型数据库，key-value 。

基于内存实现。由于是单线程，常用于高并发下的读和写。官方测试 每秒可以处理10w条数据。

![image-20210625160132219](D:\workspace\redis\README.assets\image-20210625160132219.png)

“两大维度”就是指系统维度和应用维度，“三大主线”也就是指高性能、高可靠和高可扩展（可以简称为“三高”）。

- 高性能主线，包括线程模型、数据结构、持久化、网络框架；

- 高可靠主线，包括主从复制、哨兵机制；

- 高可扩展主线，包括数据分片、负载均衡。

![image-20210625160423121](D:\workspace\redis\README.assets\image-20210625160423121.png)

#### 架构

![](D:\workspace\redis\README.assets\redis.jpg)



#### 安装

```bash
#拉取镜像
docker pull redis:latest

#本地查看
docker images

#运行
docker run -itd  --name myredis -p 6379:6379 redis

#查看
docker ps 

#进入容器
docker exec -it myredis /bin/bash

```



#### 数据类型

命令详解网站：http://doc.redisfans.com/

基本数据类型

String、list、set、hash、zset  

##### **String** 

字符串类型是 Redis 基础的数据结构，键都是字符串类型，而且其他几种数据结构都是在字符串类型 的基础上构建的。字符串类型的值可以实际可以是字符串（简单的字符串、复杂的字符串如 JSON、 XML）、数字（整形、浮点数）、甚至二进制（图片、音频、视频），但是值大不能超过 512 MB。

**String 的命令** 

**设置值**

**set key value [ex seconds] [px millseconds] [nx|xx]**

```
ex seconds：为键设置秒级过期时间，跟 setex 效果一样
px millseconds：为键设置毫秒级过期时间 
nx：键必须不存在才可以设置成功，用于添加，跟 setnx 效果一样。由于 Redis 的单线程命令处 理机制，如果多个客户端同时执行，则只			 有一个客户端能设置成功，可以用作分布式锁的一种实现。
xx：键必须存在才可以设置成功，用于更新
```

example：

```bash
127.0.0.1:6379> set username hikvision
OK
127.0.0.1:6379> get username
"hikvision"
127.0.0.1:6379> set username "hello,hikvision" # 重复设置覆盖
OK
127.0.0.1:6379> get username
"hello,hikvision"
127.0.0.1:6379> set name hik EX 1000  # 设置过期时间
OK
127.0.0.1:6379> get name
"hik"
127.0.0.1:6379> ttl name  # 查看该key 的过期时间
(integer) 988
127.0.0.1:6379> ttl name
(integer) 981
127.0.0.1:6379> set key_nx hik nx  # 只有当key不存在时才能设置成功。可与ex搭配形成锁
OK
127.0.0.1:6379> get username
"hello,hikvision"
127.0.0.1:6379> set key_nx hello,hik nx  # 键已经存在，设置失败。
(nil)
127.0.0.1:6379> get key_nx
"hik"
127.0.0.1:6379> exists key_xx  # 查看key是否存在
(integer) 0
127.0.0.1:6379> set key_xx rpq xx  # 只有当key存在时才能设置成功
(nil)
127.0.0.1:6379> set key_xx rpq 
OK
127.0.0.1:6379> set key_xx yyl xx
OK
127.0.0.1:6379> get key_xx
"yyl"


# 符合使用，用实现锁。
# 服务器返回ok，则客户端获取到锁。返回nil，即当前key被其他客户端占用，被阻塞。
# 设置的过期时间到达之后，锁将自动释放。
127.0.0.1:6379> set key_lock lock nx ex 1000 
OK
127.0.0.1:6379> 

# 锁健壮升级过程
# 1.不适用固定的字符串作为键的值，而是设置一个不可预测的长随机字符串。作为token
# 2.不使用del命令来释放锁，而是发送一个lua脚本，这个脚本只在客户端传入的值和key的token 相匹配时，才对键删除。
# 3.以上措施 可以防止持有过期锁的客户端误删现有锁的情况
	
```

**获取值**
get key ，如果不存在返回 nil

**批量设置值**
mset key value [key value...]

```bash
127.0.0.1:6379> mset key1 value1 key2 value2
OK
127.0.0.1:6379>
```



**批量获取值**
mget key [key...]
批量操作命令可以有效提高开发效率，假如没有 mget，执行 n 次 get 命令需要 n 次网络时间 + n 次命 令时间，使用 mget 只需要 1 次网络时间 + n 次命令时间。Redis 可以支持每秒数万的读写操作，但这 指的是 Redis 服务端的处理能力，对于客户端来说一次命令处理命令时间还有网络时间。因为 Redis 的 处理能力已足够高，对于开发者来说，网络可能会成为性能瓶颈。

```bash
127.0.0.1:6379> mget username age
1) "rpq"
2) "23"
```



**计数**
incr key
incr 命令用于对值做自增操作，返回结果分为三种：① 值不是整数返回错误。② 值是整数，返回自增 后的结果。③ 值不存在，按照值为 0 自增，返回结果 1。除了 incr 命令，还有自减 decr、自增指定数 字 incrby、自减指定数组 decrby、自增浮点数 incrbyfloat。

```bash
127.0.0.1:6379> set num 1
OK
127.0.0.1:6379> incr num
(integer) 2
127.0.0.1:6379> set nums aaa
OK
127.0.0.1:6379> incr nums
(error) ERR value is not an integer or out of range
127.0.0.1:6379> decr num
(integer) 1
127.0.0.1:6379> decr num
(integer) 0
127.0.0.1:6379> decr num
(integer) -1
127.0.0.1:6379> incrby num 3
(integer) 2
127.0.0.1:6379> decrby num 3
(integer) -1
127.0.0.1:6379> incrbyfloat num 3.5
"2.5"
127.0.0.1:6379> 

```

**String 的内部编码** 

- int：8 个字节的长整形 
- embstr：小于等于 39 个字节的字符串
- raw：大于 39 个字节的字符串

**String 的应用场景** 

- 缓存功能
  Redis 作为缓存层，MySQL 作为存储层，首先从 Redis 获取数据，如果失败就从 MySQL 获取并将结果 写回 Redis 并添加过期时间。
- 计数
  Redis 可以实现快速计数功能，例如视频每播放一次就用 incy 把播放数加 1。
- 共享 Session
  一个分布式 Web 服务将用户的 Session 信息保存在各自服务器，但会造成一个问题，出于负载均衡的 考虑，分布式服务会将用户的访问负载到不同服务器上，用户刷新一次可能会发现需要重新登陆。为解 决该问题，可以使用 Redis 将用户的 Session 进行集中管理，在这种模式下只要保证 Redis 是高可用和 扩展性的，每次用户更新或查询登录信息都直接从 Redis 集中获取。
- 限速
  例如为了短信接口不被频繁访问会限制用户每分钟获取验证码的次数或者网站限制一个 IP 地址不能在 一秒内访问超过 n 次。可以使用键过期策略和自增计数实现。

##### hash 

哈希类型指键值本身又是一个键值对结构，哈希类型中的映射关系叫 field-value，这里的 value 是指 field 对于的值而不是键对于的值。

**hash 的命令** 

**设置值**
hset key field value ，如果设置成功会返回 1，反之会返回 0，此外还提供了 hsetnx 命令，作用 和 setnx 类似，只是作用于由键变为 field。

**获取值**
hget key field ，如果不存在会返回 nil。
删除 field hdel key field [field...] ，会删除一个或多个 field，返回结果为删除成功 field 的个数。

```java
127.0.0.1:6379> hset person  username hello,hikvision
(integer) 1
127.0.0.1:6379> hget person username
"hello,hikvision"
127.0.0.1:6379> hset person age 23
(integer) 1
```

**计算 field 个数**
hlen key

```basj
127.0.0.1:6379> hlen person
(integer) 2
```

**判断 field 是否存在**
hexists key field ，存在返回 1，否则返回 0。

```bash
127.0.0.1:6379> hexists person username
(integer) 1
```

**获取所有的 field**
 hkeys key   返回指定哈希键的所有 field。

 hvals key ，获取指定键的所有 value。

 hgetall key  获取指定键的所有 field-value。

```java
127.0.0.1:6379> hkeys person
1) "username"
2) "age"
127.0.0.1:6379> hvals person
1) "hello,hikvision"
2) "23"
127.0.0.1:6379> 
    
127.0.0.1:6379> hgetall person
1) "username"
2) "hello,hikvision"
3) "age"
4) "23"
127.0.0.1:6379> 
```

**hash 的内部编码** 

- ziplist 压缩列表：当哈希类型元素个数和值小于配置值（默认 512 个和 64 字节）时会使用 ziplist 作为 内部实现，使用更紧凑的结构实现多个元素的连续存储，在节省内存方面比 hashtable 更优秀
- hashtable 哈希表：当哈希类型无法满足 ziplist 的条件时会使用 hashtable 作为哈希的内部实现，因为 此时 ziplist 的读写效率会下降，而 hashtable 的读写时间复杂度都为 O(1)。

**hash 的应用场景** 

缓存用户信息，每个用户属性使用一对 field-value，但只用一个键保存。
优点：简单直观，如果合理使用可以减少内存空间使用。
缺点：要控制哈希在 ziplist 和 hashtable 两种内部编码的转换，hashtable 会消耗更多内存。

##### list 

list 是用来存储多个有序的字符串，列表中的每个字符串称为元素，一个列表最多可以存储 2的32次方-1 个元 素。

可以对列表两端插入（push）和弹出（pop），还可以获取指定范围的元素列表、获取指定索引下 标的元素等。

列表是一种比较灵活的数据结构，它可以充当栈和队列的角色，在实际开发中有很多应用 场景。

**list 有两个特点：**

① 列表中的元素是有序的，可以通过索引下标获取某个元素或者某个范围内的元素列 表。

② 列表中的元素可以重复。

**list 的命令** 

**添加**
从右边插入元素： rpush key value [value...]

从左到右获取列表的所有元素： lrange 0 -1

从左边插入元素： lpush key value [value...]

向某个元素前或者后插入元素： linsert key before|after pivot value ，会在列表中找到等于 pivot 的元素，在其前或后插入一个新的元素 value。

```java
127.0.0.1:6379> rpush list a b c
(integer) 3
127.0.0.1:6379> lrange list 0 -1
1) "a"
2) "b"
3) "c"
127.0.0.1:6379> lpush list e f g
(integer) 6
127.0.0.1:6379> lrange list 0 -1
1) "g"
2) "f"
3) "e"
4) "a"
5) "b"
6) "c"
127.0.0.1:6379> linsert list before e i
(integer) 7
127.0.0.1:6379> lrange list 0 -1
1) "g"
2) "f"
3) "i"
4) "e"
5) "a"
6) "b"
7) "c"

```

**查找**

获取指定范围内的元素列表： lrange key start end ，索引从左到右的范围是 0~N-1，从右到左是 -1~-N，lrange 中的 end 包含了自身。

获取列表指定索引下标的元素： lindex key index ，获取后一个元素可以使用 lindex key -1 。

获取列表长度： llen key

```bash
127.0.0.1:6379> lrange list 0 2
1) "g"
2) "f"
3) "i"
127.0.0.1:6379> lindex list 1
"f"
127.0.0.1:6379> llen list
(integer) 7
127.0.0.1:6379> 

```

**删除**

从列表左侧弹出元素： lpop key

从列表右侧弹出元素： rpop key

删除指定元素： lrem key count value ，如果 count 大于 0，从左到右删除多 count 个元素，如 果 count 小于 0，从右到左删除多个 count 绝对值个元素，如果 count 等于 0，删除所有。 

按照索引范围修剪列表： ltrim key start end ，只会保留 start ~ end 范围的元素。

```java
127.0.0.1:6379> lrange list 0 -1
1) "g"
2) "f"
3) "i"
4) "e"
5) "a"
6) "b"
7) "c"
127.0.0.1:6379> lpop list 2
1) "g"
2) "f"
127.0.0.1:6379> rpop list 1
1) "c"
127.0.0.1:6379> lpush list e e
(integer) 6
127.0.0.1:6379> lrange list 0 -1
1) "e"
2) "e"
3) "i"
4) "e"
5) "a"
6) "b"
127.0.0.1:6379> lrem list 2 e
(integer) 2
127.0.0.1:6379> lrange list 0 -1
1) "i"
2) "e"
3) "a"
4) "b"
127.0.0.1:6379> ltrim list 2 3
OK
127.0.0.1:6379> lrange list 0 -1
1) "a"
2) "b"
127.0.0.1:6379> 

```

**修改**
修改指定索引下标的元素： lset key index newValue 。

```bash
127.0.0.1:6379> lset list 0 c
OK
127.0.0.1:6379> lrange list 0 -1
1) "c"
2) "b"
127.0.0.1:6379> 
```



**阻塞操作**

阻塞式弹出： blpop/brpop key [key...] timeout ，timeout 表示阻塞时间。

当列表为空时，如果 timeout = 0，客户端会一直阻塞，如果在此期间添加了元素，客户端会立即返 回。

如果是多个键，那么brpop会从左至右遍历键，一旦有一个键能弹出元素，客户端立即返回。

如果多个客户端对同一个键执行 brpop，那么先执行该命令的客户端可以获取弹出的值。

```bash

```



**list 的内部编码** 

- ziplist 压缩列表：跟哈希的 zipilist 相同，元素个数和大小小于配置值（默认 512 个和 64 字节）时使 用。
  linkedlist 链表：当列表类型无法满足 ziplist 的条件时会使用linkedlist。
  Redis 3.2 提供了 quicklist 内部编码，它是以一个 ziplist 为节点的 linkedlist，它结合了两者的优势， 为列表类提供了一种更为优秀的内部编码实现。

**list 的应用场景** 

- 消息队列

  Redis 的 lpush + brpop 即可实现阻塞队列，生产者客户端使用 lpush 从列表左侧插入元素，多个消费 者客户端使用 brpop 命令阻塞式地抢列表尾部的元素，多个客户端保证了消费的负载均衡和高可用 性。

- 文章列表
  每个用户有属于自己的文章列表，现在需要分页展示文章列表，就可以考虑使用列表。因为列表不但有 序，同时支持按照索引范围获取元素。每篇文章使用哈希结构存储。

​     lpush + lpop = 栈、lpush + rpop = 队列、lpush + ltrim = 优先集合、lpush + brpop = 消息队列。

##### set 

集合类型也是用来保存多个字符串元素，和列表不同的是**集合不允许有重复元素，并且集合中的元素是 无序的**，不能通过索引下标获取元素。一个集合多可以存储 232-1 个元素。Redis 除了支持集合内的 增删改查，还支持多个集合取交集、并集、差集。
**set 的命令** 

**添加元素**

sadd key element [element...] ，返回结果为添加成功的元素个数。

**删除元素**
srem key element [element...] ，返回结果为成功删除的元素个数。

**计算元素个数**
scard key ，时间复杂度为 O(1)，会直接使用 Redis 内部的遍历。

**判断元素是否在集合中**
sismember key element ，如果存在返回 1，否则返回 0。

随机从集合返回指定个数个元素
srandmember key [count] ，如果不指定 count 默认为 1。

**从集合随机弹出元素**
spop key ，可以从集合中随机弹出一个元素。

**获取所有元素**
smembers key

```bash
127.0.0.1:6379> sadd myset a b c d e f g
(integer) 7
127.0.0.1:6379> srem myset a
(integer) 1
127.0.0.1:6379> scard  myset
(integer) 6
127.0.0.1:6379> SISMEMBER myset c
(integer) 1
127.0.0.1:6379> SISMEMBER myset h
(integer) 0
127.0.0.1:6379> SRANDMEMBER 2
(nil)
127.0.0.1:6379> SRANDMEMBER myset 2
1) "e"
2) "c"
127.0.0.1:6379> SRANDMEMBER myset 2
1) "g"
2) "c"
127.0.0.1:6379> spop myset
"g"
127.0.0.1:6379> smembers myset
1) "b"
2) "c"
3) "f"
4) "e"
5) "d"
127.0.0.1:6379> 
```

**求多个集合的交集/并集/差集**

sinter key [key...]
sunion key [key...]
sdiff key [key...]

```bash
127.0.0.1:6379> sadd yourset b c d j k l
(integer) 6
127.0.0.1:6379> smembers yourset
1) "j"
2) "d"
3) "c"
4) "k"
5) "b"
6) "l"
127.0.0.1:6379> sinter myset yourset  # 求两个集合的交集
1) "b"
2) "c"
3) "d"
127.0.0.1:6379> sunion myset yourset # 集合的并集
1) "k"
2) "b"
3) "c"
4) "f"
5) "e"
6) "l"
7) "j"
8) "d"
127.0.0.1:6379> sdiff myset yourset # 以前者为标准两个集合的差集
1) "e"
2) "f"
127.0.0.1:6379> sdiff  yourset myset
1) "k"
2) "l"
3) "j"
127.0.0.1:6379> 

```



**保存交集、并集、差集的结果**

sinterstore/sunionstore/sdiffstore destination key [key...]
集合间运算在元素较多情况下比较耗时，Redis 提供这三个指令将集合间交集、并集、差集的结果保存 在 destination key 中。

```bash
127.0.0.1:6379> SDIFFSTORE newset myset yourset # 将两个集合的差集存入到一个新的集合中
(integer) 2
127.0.0.1:6379> smembers newset 
1) "e"
2) "f"
127.0.0.1:6379> 

```



**set 的内部编码** 

- intset 整数集合：当集合中的元素个数小于配置值（默认 512 个时），使用 intset。

- hashtable 哈希表：当集合类型无法满足 intset 条件时使用 hashtable。当某个元素不为整数时，也会 使用 hashtable。

**set 的应用场景** 
set 比较典型的使用场景是标签，例如一个用户可能与娱乐、体育比较感兴趣，另一个用户可能对历史、新闻比较感兴趣，这些兴趣点就是标签。这些数据对于用户体验以及增强用户黏度比较重要。
sadd = 标签、spop/srandmember = 生成随机数，比如抽奖、sadd + sinter = 社交需求。

##### **zset** 

**有序集合保留了集合不能有重复成员的特性，不同的是可以排序。**但是它和列表使用索引下标作为排序 依据不同的是，他给每个元素设置一个分数（score）作为排序的依据。有序集合提供了获取指定分数 和元素查询范围、计算成员排名等功能。

**zset 的命令** 

**添加成员**
zadd key score member [score member...] ，返回结果是成功添加成员的个数
Redis 3.2 为 zadd 命令添加了 nx、xx、ch、incr 四个选项： nx：member 必须不存在才可以设置成功，用于添加。 xx：member 必须存在才能设置成功，用于更新。 ch：返回此次操作后，有序集合元素和分数变化的个数。 incr：对 score 做增加，相当于 zincrby。 zadd 的时间复杂度为 O(logn)，sadd 的时间复杂度为 O(1)。

**计算成员个数**
zcard key ，时间复杂度为 O(1)。

**计算某个成员的分数**
zscore key member ，如果不存在则返回 nil。

**计算成员排名**
zrank key member ，从低到高返回排名。
zrevrank key member ，从高到低返回排名。

**删除成员**
zrem key member [member...] ，返回结果是成功删除的个数。

**增加成员的分数**
zincrby key increment member

**返回指定排名范围的成员**
zrange key start end [withscores] ，从低到高返回
zrevrange key start end [withscores] ， 从高到底返回

**返回指定分数范围的成员**
zrangebyscore key min max [withscores] [limit offset count] ，从低到高返回
zrevrangebyscore key min max [withscores] [limit offset count] ， 从高到底返回

**返回指定分数范围成员个数**
zcount key min max

**删除指定分数范围内的成员**
zremrangebyscore key min max

**交集和并集**
zinterstore/zunionstore destination numkeys key [key...] [weights weight
[weight...]] [aggregate sum|min|max]
destination ：交集结果保存到这个键 numkeys ：要做交集计算键的个数 key ：需要做交集计算的键 weight ：每个键的权重，默认 1 aggregate sum|min|max ：计算交集后，分值可以按和、小值、大值汇总，默认 sum。

**zset 的内部编码** 
ziplist 压缩列表：当有序集合元素个数和值小于配置值（默认128 个和 64 字节）时会使用 ziplist 作为 内部实现。
skiplist 跳跃表：当 ziplist 不满足条件时使用，因为此时 ziplist 的读写效率会下降。

**zset 的应用场景** 
有序集合的典型使用场景就是排行榜系统，例如用户上传了一个视频并获得了赞，可以使用 zadd 和 zincrby。如果需要将用户从榜单删除，可以使用 zrem。如果要展示获取赞数多的十个用户，可以使 用 zrange。

#### 单线程

官方FAQ表示，因为Redis是基于内存的操作，CPU不是Redis的瓶颈，Redis的瓶颈最有可能是机器内存 的大小或者网络带宽。既然单线程容易实现，而且CPU不会成为瓶颈，那就顺理成章地采用单线程的方 案了（毕竟采用多线程会有很多麻烦！）

#### 特性

- 完全基于内存，绝大部分请求是纯粹的内存操作，非常快速。数据存在内存中，类似于 HashMap，HashMap的优势就是查找和操作的时间复杂度都是O(1)； 
- 非阻塞io多路复用
  - IO多路复用：IO 多路复用是一种同步IO模型，实现一个线程可以监视多个文件句柄； 一旦某个文件句柄就绪，就能够通知应用程序进行相应的读写操作； 没有文件句柄就绪就会阻塞应用程序，交出CPU
- 采用单线程，避免了不必要的上下文切换和竞争条件，也不存在多进程或者多线程导致的切换而消 耗 CPU，不用去考虑各种锁的问题，不存在加锁释放锁操作，没有因为可能出现死锁而导致的性能消耗
- 高效的数据结构（哈希表、跳表）

#### 持久化

持久化就是把内存的数据写到磁盘中去，防止服务宕机了内存数据丢失。

redis使用持久化机制，来解决这种问题。就是==采用不同的策略，将内存中的数据在一定时间内写的硬盘 上形成文件。等到下次启动redis时，将文件重新读取到redis中。==

**RDB** 
redis默认的持久化机制。==按照一定的时间将内存中的数据以快照的形式存入到硬盘中==，对应产生的数据 文件为dump.rdb。通过配置文件中的save参数来定义快照的周期。

缺点：在对数据频繁读写时，假如设备出现故障，最后一次持久化以后的对数据写的数据将会丢失。

**AOF** 

AppendOnlyFile ：==将redis执行的每次写命令记录到单独的日志文件中==，当重启redis会重新从持久化的 日志文件中恢复数据。

**在实际开发中，会使用rdb+aof的持久化策略。保证数据的完整性。当两种方式同时开启时，数据恢复 Redis会优先选择AOF恢复。** 

#### 过期键的删除策略

Redis 是key-value型数据库，我们可以设置redis中缓存的key的过期时间。

Redis的过期策略是 当Redis中缓存的key过期了，Redis如何处理。

reids中有个设置key时间过期的功能，即对存储在redis数据中的值可以设置一个过期时间。

```bash
#设置该key 10s 后 过期

set username hikvision
expire 10 

set username 10 hikvision 
```

当一个key设置过期时间后，到了过期时间之后将会被删除。

策略：

- 立即删除 ：在设置key的过期时间后，创建一个回调事件，当过期时间达到时，由时间处理器自动执行键的删除操作。即时间到了之后立马从内存中删除
- 定时删除:   每隔一段时间，删除一批过期键
- 惰性删除:    不处理。在取值的时候，先检查此key是否已经过期，如果过期了就删除它。返回nil。没 有就返回值。 

#### 数据淘汰策略

#### **springboot 整合redis**

1.建立springboot 工程 导入相关依赖

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.16.18</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.16.18</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
```

2.配置数据源信息

```properties
spring.redis.host=47.95.37.191
spring.redis.port=6379
```

3.自定义redisTemplate 

springboot 自动配置了redis，配置类为RedisTemplate，自定义属性类RedisProperties

```java
   @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        // Json 序列化配置
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        // ObjectMapper 转译
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance , ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
```

4.引入并使用（对象在读写的时候需要序列化）

redisTemplate 对redis的命令进行了封装。

```java
@RestController
@RequestMapping("/hik")
@Slf4j
public class RedisController {

    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping("/get")
    public User get(){
        User user = (User) redisTemplate.opsForValue().get("username"); 
        return user;
    }

    @RequestMapping("/set")
    public String set(@RequestBody User user){
        log.info("user: {}",user);
        redisTemplate.opsForValue().set("username",user);
        return  "插入成功";
    }

}

```

5.开发中经常对redisTemplate 进行再次封装，提高开发效率。

```java
package com.hikvision.redis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author renpeiqian
 * @date 2021/6/25 15:50
 */
@Component
public class RedisUtils {
        @Autowired
        private RedisTemplate<String, Object> redisTemplate;

        /**
         * 指定缓存失效时间
         *
         * @param key  键
         * @param time 时间(秒)
         * @return
         */
        public boolean expire(String key, long time) {
            try {
                if (time > 0) {
                    redisTemplate.expire(key, time, TimeUnit.SECONDS);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 根据 key 获取过期时间
         *
         * @param key 键(不能为 Null)
         * @return 时间(秒) 返回0代表永久有效
         */
        public long getExpire(String key) {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        }

        /**
         * 判断 key 是否存在
         *
         * @param key 键(不能为 Null)
         * @return true 存在 false 不存在
         */
        public boolean hashKey(String key) {
            try {
                return redisTemplate.hasKey(key);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 删除缓存
         *
         * @param key 可以传一个值 或多个
         */
        public void del(String... key) {
            if (key != null && key.length > 0) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }


        //==================================String====================================

        /**
         * 普通缓存获取
         *
         * @param key 键
         * @return 值
         */
        public Object get(String key) {
            return key == null ? null : redisTemplate.opsForValue().get(key);
        }

        /**
         * 普通缓存放入
         *
         * @param key   键
         * @param value 值
         * @return true 成功 false 失败
         */
        public boolean set(String key, Object value) {
            try {
                redisTemplate.opsForValue().set(key, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 普通缓存放入并设置时间
         *
         * @param key   键
         * @param value 值
         * @param time  时间(秒) time > 0 若 time <= 0 将设置无限期
         * @return true 成功 false 失败
         */
        public boolean set(String key, Object value, long time) {
            try {
                if (time > 0) {
                    redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
                } else {
                    set(key, value);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 递增
         *
         * @param key   键
         * @param delta 要增加几(大于0)
         * @return
         */
        public long incr(String key, long delta) {
            if (delta < 0) {
                throw new RuntimeException("递增因子必须大于0");
            }
            return redisTemplate.opsForValue().increment(key, delta);
        }

        /**
         * 递减
         *
         * @param key   键
         * @param delta 要减少几(小于0)
         * @return
         */
        public long decr(String key, long delta) {
            if (delta < 0) {
                throw new RuntimeException("递减因子必须大于0");
            }
            return redisTemplate.opsForValue().decrement(key, delta);
        }


        // ================================Map=================================

        /**
         * HashGet
         *
         * @param key  键 不能为null
         * @param item 项 不能为null
         */
        public Object hget(String key, String item) {
            return redisTemplate.opsForHash().get(key, item);
        }

        /**
         * 获取hashKey对应的所有键值
         *
         * @param key 键
         * @return 对应的多个键值
         */
        public Map<Object, Object> hmget(String key) {
            return redisTemplate.opsForHash().entries(key);
        }

        /**
         * HashSet
         *
         * @param key 键
         * @param map 对应多个键值
         */
        public boolean hmset(String key, Map<String, Object> map) {
            try {
                redisTemplate.opsForHash().putAll(key, map);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        /**
         * HashSet 并设置时间
         *
         * @param key  键
         * @param map  对应多个键值
         * @param time 时间(秒)
         * @return true成功 false失败
         */
        public boolean hmset(String key, Map<String, Object> map, long time) {
            try {
                redisTemplate.opsForHash().putAll(key, map);
                if (time > 0) {
                    expire(key, time);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        /**
         * 向一张hash表中放入数据,如果不存在将创建
         *
         * @param key   键
         * @param item  项
         * @param value 值
         * @return true 成功 false失败
         */
        public boolean hset(String key, String item, Object value) {
            try {
                redisTemplate.opsForHash().put(key, item, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 向一张hash表中放入数据,如果不存在将创建
         *
         * @param key   键
         * @param item  项
         * @param value 值
         * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
         * @return true 成功 false失败
         */
        public boolean hset(String key, String item, Object value, long time) {
            try {
                redisTemplate.opsForHash().put(key, item, value);
                if (time > 0) {
                    expire(key, time);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        /**
         * 删除hash表中的值
         *
         * @param key  键 不能为null
         * @param item 项 可以使多个 不能为null
         */
        public void hdel(String key, Object... item) {
            redisTemplate.opsForHash().delete(key, item);
        }


        /**
         * 判断hash表中是否有该项的值
         *
         * @param key  键 不能为null
         * @param item 项 不能为null
         * @return true 存在 false不存在
         */
        public boolean hHasKey(String key, String item) {
            return redisTemplate.opsForHash().hasKey(key, item);
        }


        /**
         * hash递增 如果不存在,就会创建一个 并把新增后的值返回
         *
         * @param key  键
         * @param item 项
         * @param by   要增加几(大于0)
         */
        public double hincr(String key, String item, double by) {
            return redisTemplate.opsForHash().increment(key, item, by);
        }


        /**
         * hash递减
         *
         * @param key  键
         * @param item 项
         * @param by   要减少记(小于0)
         */
        public double hdecr(String key, String item, double by) {
            return redisTemplate.opsForHash().increment(key, item, -by);
        }


        // ============================set=============================

        /**
         * 根据key获取Set中的所有值
         *
         * @param key 键
         */
        public Set<Object> sGet(String key) {
            try {
                return redisTemplate.opsForSet().members(key);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        /**
         * 根据value从一个set中查询,是否存在
         *
         * @param key   键
         * @param value 值
         * @return true 存在 false不存在
         */
        public boolean sHasKey(String key, Object value) {
            try {
                return redisTemplate.opsForSet().isMember(key, value);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        /**
         * 将数据放入set缓存
         *
         * @param key    键
         * @param values 值 可以是多个
         * @return 成功个数
         */
        public long sSet(String key, Object... values) {
            try {
                return redisTemplate.opsForSet().add(key, values);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }


        /**
         * 将set数据放入缓存
         *
         * @param key    键
         * @param time   时间(秒)
         * @param values 值 可以是多个
         * @return 成功个数
         */
        public long sSetAndTime(String key, long time, Object... values) {
            try {
                Long count = redisTemplate.opsForSet().add(key, values);
                if (time > 0) {
                    expire(key, time);
                }
                return count;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }


        /**
         * 获取set缓存的长度
         *
         * @param key 键
         */
        public long sGetSetSize(String key) {
            try {
                return redisTemplate.opsForSet().size(key);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }


        /**
         * 移除值为value的
         *
         * @param key    键
         * @param values 值 可以是多个
         * @return 移除的个数
         */

        public long setRemove(String key, Object... values) {
            try {
                Long count = redisTemplate.opsForSet().remove(key, values);
                return count;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        // ===============================list=================================

        /**
         * 获取list缓存的内容
         *
         * @param key   键
         * @param start 开始
         * @param end   结束 0 到 -1代表所有值
         */
        public List<Object> lGet(String key, long start, long end) {
            try {
                return redisTemplate.opsForList().range(key, start, end);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        /**
         * 获取list缓存的长度
         *
         * @param key 键
         */
        public long lGetListSize(String key) {
            try {
                return redisTemplate.opsForList().size(key);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }


        /**
         * 通过索引 获取list中的值
         *
         * @param key   键
         * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
         */
        public Object lGetIndex(String key, long index) {
            try {
                return redisTemplate.opsForList().index(key, index);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        /**
         * 将list放入缓存
         *
         * @param key   键
         * @param value 值
         */
        public boolean lSet(String key, Object value) {
            try {
                redisTemplate.opsForList().rightPush(key, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        /**
         * 将list放入缓存
         *
         * @param key   键
         * @param value 值
         * @param time  时间(秒)
         */
        public boolean lSet(String key, Object value, long time) {
            try {
                redisTemplate.opsForList().rightPush(key, value);
                if (time > 0) {
                    expire(key, time);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }


        /**
         * 将list放入缓存
         *
         * @param key   键
         * @param value 值
         * @return
         */
        public boolean lSet(String key, List<Object> value) {
            try {
                redisTemplate.opsForList().rightPushAll(key, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }


        /**
         * 将list放入缓存
         *
         * @param key   键
         * @param value 值
         * @param time  时间(秒)
         * @return
         */
        public boolean lSet(String key, List<Object> value, long time) {
            try {
                redisTemplate.opsForList().rightPushAll(key, value);
                if (time > 0) {
                    expire(key, time);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        /**
         * 根据索引修改list中的某条数据
         *
         * @param key   键
         * @param index 索引
         * @param value 值
         * @return
         */

        public boolean lUpdateIndex(String key, long index, Object value) {
            try {
                redisTemplate.opsForList().set(key, index, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        /**
         * 移除N个值为value
         *
         * @param key   键
         * @param count 移除多少个
         * @param value 值
         * @return 移除的个数
         */

        public long lRemove(String key, long count, Object value) {
            try {
                Long remove = redisTemplate.opsForList().remove(key, count, value);
                return remove;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }

        }

        // ===============================HyperLogLog=================================

        public long pfadd(String key, String value) {
            return redisTemplate.opsForHyperLogLog().add(key, value);
        }

        public long pfcount(String key) {
            return redisTemplate.opsForHyperLogLog().size(key);
        }

        public void pfremove(String key) {
            redisTemplate.opsForHyperLogLog().delete(key);
        }

        public void pfmerge(String key1, String key2) {
            redisTemplate.opsForHyperLogLog().union(key1, key2);
        }


    
}

```

### 