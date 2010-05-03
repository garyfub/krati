package krati.sos;

import java.io.IOException;

/**
 * ObjectStoreAgent:
 * 
 * An agent that wraps an ObjectStore can have inbound and outbound ObjectHandler(s).
 * The inbound handler is associated with the put method. It is called on an inbound object before the object is passed down to the underlying ObjectStore.
 * The outbound handler is associated with the get method. It is called on an outbound object before the object is returned back to the ObjectStore visitor.
 * Either inbound or outbound handlers does not affect the delete method.
 * 
 * <pre>
 *    get(K key)
 *      + get object from the underlying store
 *      + Call the outbound handler on the object
 *      + return the object
 *  
 *    put(K key, V value)
 *      + Call the inbound handler on the value object
 *      + delegate operation put to the underlying store
 * 
 * </pre>
 *  
 * @author jwu
 *
 * @param <K> Key 
 * @param <V> Value
 */
public class ObjectStoreAgent<K, V> implements ObjectStore<K, V>
{
    protected ObjectStore<K, V> _store;
    protected ObjectHandler<V> _inboundHandler;
    protected ObjectHandler<V> _outboundHandler;
    
    public ObjectStoreAgent(ObjectStore<K,V> store,
                            ObjectHandler<V> inboundHandler,
                            ObjectHandler<V> outboundHandler)
    {
        this._store = store;
        this._inboundHandler = inboundHandler;
        this._outboundHandler = outboundHandler;
    }
    
    public ObjectStore<K, V> getObjectStore()
    {
        return _store;
    }
    
    public ObjectHandler<V> getInboundHandler()
    {
        return _inboundHandler;
    }
    
    public ObjectHandler<V> getOutboundHandler()
    {
        return _outboundHandler;
    }
    
    @Override
    public boolean delete(K key) throws Exception
    {
        synchronized(_store)
        {
            return _store.delete(key);
        }
    }
    
    @Override
    public V get(K key)
    {
        V value = _store.get(key);
        if(value != null && _outboundHandler != null && _outboundHandler.getEnabled())
        {
            _outboundHandler.process(value);
        }
        return value;
    }
    
    @Override
    public boolean put(K key, V value) throws Exception
    {
        if(value != null && _inboundHandler != null && _inboundHandler.getEnabled())
        {
            _inboundHandler.process(value);
        }
        
        synchronized(_store)
        {
            return _store.put(key, value);
        }
    }
    
    /**
     * Persists this object store.
     * 
     * @throws IOException
     */
    @Override
    public void persist() throws IOException
    {
        synchronized(_store)
        {
            _store.persist();
        }
    }
    
    /**
     * Clears this object store by removing all the persisted data permanently.
     * 
     * @throws IOException
     */
    public void clear() throws IOException
    {
        synchronized(_store)
        {
            _store.clear();
        }
    }
}
