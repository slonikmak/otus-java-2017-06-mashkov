package ru.otus.simplecache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class SimpleCacheEngineTest {



    @BeforeEach
    void setUp(){
    }

    @AfterEach
    void tearDown(){

    }

    @Test
    void putMaxElem() {
        SimpleCacheEngine<Integer, TestObj> cache = new CacheFactory<Integer, TestObj>()
                .getEternalCache(3);
        int size = 10;
        for (int i = 0; i < size; i++) {
            cache.put(i, new TestObj(i, "name"+i));
        }

        cache.get(0);
        cache.get(9);
        Assertions.assertEquals(1, cache.getMissCount());
        Assertions.assertEquals(1, cache.getHitCount());


        //System.out.println(cacheEngine.get(3).isPresent());
    }

    @Test
    void putLiveTime(){
        SimpleCacheEngine<Integer, TestObj> cache = new CacheFactory<Integer, TestObj>()
                .getLiveCache(3, 100);
        int size = 10;
        for (int i = 0; i < size; i++) {
            cache.put(i, new TestObj(i, "name"+i));
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cache.get(1);
        cache.get(9);
        Assertions.assertEquals(2, cache.getMissCount());
        //Assertions.assertEquals(1, cache.getHitCount());

    }

    @Test
    void putIdleTime(){
        SimpleCacheEngine<Integer, TestObj> cache = new CacheFactory<Integer, TestObj>()
                .getIdleCache(3, 200);
        int size = 3;
        for (int i = 0; i < size; i++) {
            cache.put(i, new TestObj(i, "name"+i));
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cache.get(0);
        cache.get(1);
        Assertions.assertEquals(2, cache.getHitCount());
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cache.get(0);
        cache.get(1);
        cache.get(2);
        Assertions.assertEquals(4, cache.getHitCount());
        Assertions.assertEquals(1, cache.getMissCount());

    }

    @Test
    void softRefTest(){
        int max = 1000;

        SimpleCacheEngine<Integer, BigObj> cache = new CacheFactory<Integer, BigObj>()
                .getEternalCache(max);

        for (int i = 0; i < max; i++) {
            cache.put(i, new BigObj());
        }

        System.gc();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < max; i++) {
            cache.get(i);
        }

        Assertions.assertTrue(max>cache.getHitCount());



    }


    static class TestObj{
        int id;
        String name;

        TestObj(int id, String name){
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestObj)) return false;

            TestObj testObj = (TestObj) o;

            if (id != testObj.id) return false;
            return name != null ? name.equals(testObj.name) : testObj.name == null;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "TestObj{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    static class BigObj{
        long[] longs;

        BigObj(){
            longs = new long[1000*1000];
        }
    }



}