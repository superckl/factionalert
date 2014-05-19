package me.superckl.actionalert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.NonNull;

public class ModuleManager<K> {

	@Getter
	private final Map<ModuleType, K> modules = new ConcurrentHashMap<ModuleType, K>();

	public void addModule(final ModuleType type, @NonNull final K module){
		if(this.modules.containsKey(type))
			throw new IllegalArgumentException("A module with that type is already present!");
		this.modules.put(type, module);
	}

	public K getByType(final ModuleType type){
		return this.modules.get(type);
	}

}
