# leveldb的java实现
leveldb是一个key/value型的单机存储引擎，由google开发，是leveling+分区实现的LSM典型代表


特性
- key、value支持任意的byte类型数组，不单单支持字符串
- leveldb是一个持久化存储的KV系统，将大部分数据存储到磁盘上
- 按照记录key值顺序存储数据，并且LevleDb支持按照用户定义的比较函数进行排序
- 操作接口简单，包括写/读记录以及删除记录，也支持针对多条操作的原子批量操作
- 支持数据快照（snapshot）功能，使得读取操作不受写操作影响，可以在读操作过程中始终看到一致的数据
- 支持数据压缩(snappy压缩)操作，有效减小存储空间、并增快IO效率
- LSM典型实现，适合写多读少

leveldb的基本框架，几大关键组件，如下图


leveldb是一种基于operation log的文件系统，是Log-Structured-Merge Tree的典型实现。  


由于采用了op log，它可以把随机的磁盘写操作，变成对op log的append操作，因此提高了IO效率，最新的数据则存储在内存memtable中。 


当op log文件大小超过限定值时，就定时做check point。leveldb会生成新的log文件和memtable，后台调度会将Immutable Memtable的数据导出到磁盘，
形成一个新的SSTable文件。SSTable就是由内存中的数据不断导出并进行Compaction操作后形成的，而且而且SSTable的所有文件是一种层级结构，
第一层为Level 0，第二层为Level 1，依次类推，层级逐渐增高，这也是为何称之为leveldb的原因。

## 一些约定
### 字节序
leveldb对于数字的存储是**little-endian(小端序)**，在把int32或者int64转换为char*的函数中，是按照先低位再高位的的顺序存放的。


字节序是处理器架构的特性，比如一个16位的整数，他是由2个字节组成。内存中存储2个字节有两种方法：
- 将低序字节存储在起始地址，称为小端；
- 将高序字节存储在起始地址，称为大端；
  ![9ea1b7d9-3d88-4348-be57-69ac813dce00.png](https://s2.loli.net/2023/04/05/Ge6TMArywKJCsW3.png)

### 编码
leveldb中分为定长和变长编码(VarInt)，其中变长编码目的是为了减少空间占用。
其基本思想是：每一个Byte最高bit用0/1表示该整数是否结束，用剩余7bit表示实际的数值，在protobuf中被广泛使用。  
见函数char* EncodeVarint32(char* dst, uint32_t v)
> 说明：当uint32数值较小的时候，其实一个字节就够了。  
例如：数字1的uint32二进制为 00000001 00000000 00000000 00000000，需要4个字节
当采用VarInt存储时为：00000001 只需要1个字节

**在操作log中使用的是Fixed存储格式**。

### 字符比较
是基于unsigned char的，而非char。

## 基本数据结构
核心数据结构：LRUCache、跳跃表(Skip list)
### Slice
slice是leveldb中自定义的字符串处理类  
使用原因主要是因为标准库中的string：
- 默认语意为拷贝，会损失性能(在可预期的条件下，指针传递即可)
- 标准库不支持remove_prefix和starts_with等函数，不太方便

1. 包括length和一个指向外部字节数组的指针
2. 和string一样，允许字符串中包含’\0’。

![image.png](https://s2.loli.net/2023/04/05/hQtUkN3ydTnGZWI.png)

### status
用于记录leveldb中状态信息，保存错误码和对应的字符串错误信息(不过不支持自定义)。其基本组成  


![11146444-4ecd-4f9d-9a34-b2a7348ebb8a.png](https://s2.loli.net/2023/04/05/VtxP8o4kFfAwOWg.png)

### Arena
leveldb的简单的内存池，它所作的工作十分简单，申请内存时，将申请到的内存块放入std::vector blocks_中，
在Arena的生命周期结束后，统一释放掉所有申请到的内存，内部结构如图所示。

![leveldb2.webp](https://s2.loli.net/2023/04/05/uRcZoqUFaiz8r9A.webp)


Arena主要提供了两个申请函数：其中一个直接分配内存，另一个可以申请对齐的内存空间。  
Arena没有直接调用delete/free函数，而是由Arena的析构函数统一释放所有的内存。  
应该说这是和leveldb特定的应用场景相关的，比如一个memtable使用一个Arena，当memtable被释放时，由Arena统一释放其内存。

### 跳跃表(skip list)
跳跃表 是一种可以代替**平衡树**的数据结构。跳跃表利用概率保证平衡，平衡树采用严格的旋转（比如平衡二叉树有左旋右旋）来保证平衡，
因此跳跃表比较容易实现，而且相比平衡树有着较高的运行效率。

从概率上保持数据结构的平衡比显式的保持数据结构平衡要简单的多。对于大多数应用，用跳跃表要比用树更自然，算法也会相对简单。
由于跳跃表比较简单，实现起来会比较容易，虽然和平衡树有着相同的时间复杂度(O(logn))，但是跳跃表的常数项相对小很多。
跳跃表在空间上也比较节省。一个节点平均只需要1.333个指针（甚至更少），并且不需要存储保持平衡的变量。






  
