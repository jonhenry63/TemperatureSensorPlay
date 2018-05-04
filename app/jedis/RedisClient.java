package jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;

public class RedisClient {

    private JedisPool pool;

    public RedisClient() {
        try {
            pool = new JedisPool(new JedisPoolConfig(), "localhost");
        } catch (Exception e) {
            // do nothing
        }
    }

    public Long zadd(String key, double score, String member) {
        Jedis jedis = claimResource();
        try {
            return jedis.zadd(key, score, member);
        } finally {
            returnResource(jedis);
        }
    }

    public Set<String> zrangebyscore(String key, double min, double max) {
        Jedis jedis = claimResource();
        try {
            return jedis.zrangeByScore(key, min, max);
        } finally {
            returnResource(jedis);
        }
    }

    public Set<String> zrange(String key, int start, int end) {
        Jedis jedis = claimResource();
        try {
            return jedis.zrange(key, start, end);
        } finally {
            returnResource(jedis);
        }
    }

    public void destroy() {
        pool.destroy();
    }

    private Jedis claimResource() {
        return pool.getResource();
    }

    private void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

}