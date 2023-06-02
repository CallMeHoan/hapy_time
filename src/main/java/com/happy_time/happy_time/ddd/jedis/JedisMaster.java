package com.happy_time.happy_time.ddd.jedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.Map;

@Component
public class JedisMaster {

    @Autowired
    private Environment env;

    public static String COLON = ":";

    public static class JedisPrefixKey {
        public static String ranking_tenant_agent = "ranking_tenant_agent";

        public static String ip_config_tenant = "ip_config_tenant";

        public static String agent_tenant = "agent_tenant";
    }

    public static class TimeUnit {
        public static long one_day = 24L * 60 * 60 * 1000;
    }
    public Jedis connect() {
        try {
            String connectionString = env.getProperty("redis.uri");
            return new Jedis(connectionString);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("Connect to redis failed");
        }
    }

    public String hget(String key) {
        Jedis jedis = this.connect();
        String res = jedis.get(key);
        jedis.close();
        return res;
    }

    public Map<String, String> hgetAll(String key) {
        Jedis jedis = this.connect();
        Map<String, String> res = jedis.hgetAll(key);
        jedis.close();
        return res;
    }

    public Long hSet(String key, String field,String value) {
        Jedis jedis = this.connect();
        Long res = jedis.hset(key, field, value);
        jedis.close();
        return res;
    }

    public Long hSetAll(String key, Map<String, String> values) {
        Jedis jedis = this.connect();
        Long res = jedis.hset(key, values);
        jedis.close();
        return res;
    }

    public Long hDel(String key) {
        Jedis jedis = this.connect();
        Long res = jedis.del(key);
        jedis.close();
        return res;
    }
}
