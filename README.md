# Redlock
基于Redis的分布式锁。

### Lock
创建一个RedLock全局对象，默认连接localhost:6379：

	RedLock redLock = RedLock.create();

获取一个锁：

	Lock lock = redLock.lock("test");

你也可以指定一个时间参数从而设置这个锁的过期时间。值得注意的是，如果你使用lock这个API去获取分布式锁，线程会真正的BLOCK住如果这个锁目前被其他线程、进程甚至其他应用占有。

如果你只是想尝试获取锁并在锁被别人持有时直接返回NULL，尝试下tryLock方法：

	Lock lock = redLock.tryLock("test");

释放一个锁：

	redLock.unlock(lock);

## Build And Run
直接通过Maven install然后在dependency里配置即可。


