# entry-synced-map

The entry synced map allows multiple threads accessing different entries simultaneously, 
while each entry can be manipulated by only one thread at a time.

The test scenario is that **_thread0_** and **_thread1_** both access entry **key1**, 
and **_thread2_** access entry **key2**. Either of **_thread0_** and **_thread1_** 
which does not able to lock **key1** first needs to wait until the other thread to finish. 
Meanwhile, **_thread2_** can access **key2** freely.

The normal usage of entry synced `map` could be:
```
map.lockEntry(key)
// process the entry ...
map.unlockEntry(key)
```
