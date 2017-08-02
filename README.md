# Redlock
基于Redis的分布式锁。

### Lock
创建一个RedLock全局对象，默认连接localhost:6379：

	RedLock redLock = RedLock.create();

获取一个锁：

	Lock lock = redLock.getLock("test");
	lock.lock();	

你也可以指定一个时间参数从而设置这个锁的过期时间。值得注意的是，如果你使用lock这个API去获取分布式锁，线程会真正的BLOCK住如果这个锁目前被其他线程、进程甚至其他应用占有。

如果你只是想尝试获取锁并在锁被别人持有时直接返回失败，尝试下tryLock方法：

	lock.tryLock();

释放一个锁：

	lock.unlock();
	
	
强行释放一个锁，普通的释放锁操作只是让你的holdCount -1，也就是说如果你lock了两次，需要unlock两次才会真正完全释放他，而forceUnlock能直接一次性释放干净：

	lock.forceUnlock();

当然是在你持有这个锁的前提下。

## Build And Run
直接通过Maven install然后在dependency里配置即可。


