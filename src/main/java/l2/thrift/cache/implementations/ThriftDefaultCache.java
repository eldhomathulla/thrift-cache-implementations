package l2.thrift.cache.implementations;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.cache.CacheConfiguration;
import org.apache.thrift.cache.DefaultCache;
import org.apache.thrift.cache.DependentFunctionActionHolder;
import org.apache.thrift.cache.TCacheKey;
import org.apache.thrift.cache.ThriftLockFactory;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public abstract class ThriftDefaultCache extends DefaultCache {

	private ObjectMapper objectMapper;

	public ThriftDefaultCache(CacheConfiguration cacheConfiguration,
			Function<String, List<DependentFunctionActionHolder>> dependentFunctionActionHolderListSupplier,
			Object... ifaces) {
		super(cacheConfiguration, dependentFunctionActionHolderListSupplier, ifaces);
		initializeObjectMapper();
	}

	private void initializeObjectMapper() {
		objectMapper = new ObjectMapper();
		objectMapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
		objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE)
				.setVisibility(PropertyAccessor.FIELD, Visibility.PUBLIC_ONLY)
				.setVisibility(PropertyAccessor.GETTER, Visibility.PUBLIC_ONLY)
				.setVisibility(PropertyAccessor.SETTER, Visibility.PUBLIC_ONLY);
	}

	public ThriftDefaultCache(CacheConfiguration cacheConfiguration, Object... ifaces) {
		this(cacheConfiguration, null, ifaces);
	}

	public ThriftDefaultCache(CacheConfiguration cacheConfiguration, ThriftLockFactory thriftLockFactory,
			Function<String, List<DependentFunctionActionHolder>> dependentFunctionActionHolderListSupplier,
			Object... ifaces) {
		super(cacheConfiguration, thriftLockFactory, dependentFunctionActionHolderListSupplier, ifaces);
		initializeObjectMapper();
	}

	protected String serialize(Object obj) throws TException {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new TException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	protected TBase deSerializeTBase(String tbase) {
		return deSerialize(tbase, TBase.class);
	}

	protected TCacheKey deSerializeTCacheKey(String tCacheKey) {
		return deSerialize(tCacheKey, TCacheKey.class);
	}

	private <T> T deSerialize(String value, Class<T> tClass) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.readValue(value, tClass);
		} catch (IOException e) {
			throw new RuntimeException(value);
		}
	}

}
