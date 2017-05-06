package ax25irc.ircd.util;

import java.util.concurrent.ConcurrentHashMap;

public class CaseIMap<K, V> extends ConcurrentHashMap<K, V>
{

    private static final long serialVersionUID = -5486480749633612793L;

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value)
    {
        if (key instanceof String)
        {
            String key_s = (String) key;
            key_s = key_s.toLowerCase();
            return super.put((K) key_s, value);
        }

        return super.put(key, value);
    }

    @Override
    public V remove(Object key)
    {
        if (key instanceof String)
        {
            String key_s = (String) key;
            key_s = key_s.toLowerCase();
            return super.remove((Object) key_s);
        }

        return super.remove(key);
    }

    @Override
    public V get(Object key)
    {
        if (key instanceof String)
        {
            String key_s = (String) key;
            key_s = key_s.toLowerCase();
            return super.get((Object) key_s);
        }

        return super.get(key);
    }

    @Override
    public boolean containsKey(Object key)
    {
        if (key instanceof String)
        {
            String key_s = (String) key;
            key_s = key_s.toLowerCase();
            return super.containsKey((Object) key_s);
        }

        return super.containsKey(key);
    }

}
