package dev.priveweb.core.mapper.impl;

import dev.priveweb.core.mapper.MappingConfiguration;
import dev.priveweb.core.mapper.MappingResolver;
import dev.priveweb.core.server.impl.PriveWebServer;
import dev.priveweb.core.http.request.RequestMethod;
import dev.priveweb.core.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.StringJoiner;

public class MappingConfigurationImpl implements MappingConfiguration {

	private final PriveWebServer server;
	private final MappingResolver mappingResolver;

	public MappingConfigurationImpl(PriveWebServer server, MappingResolver mappingResolver) {
		this.server = server;
		this.mappingResolver = mappingResolver;
	}

	@Override
	public void map(@NotNull String url, @NotNull RequestMethod requestMethod, @Nullable Method method) {
		if(method == null) {
			mappingResolver.getMappingHandlerMap().remove(new Pair<>(url, requestMethod));
			mappingResolver.getMappingHandlerMap().remove(new Pair<>(url + "/", requestMethod));
		} else {
			mappingResolver.getMappingHandlerMap().compute(new Pair<>(url, requestMethod), (k1, v1) -> {
				if(v1 == null) {
					String url2 = url;
					if(!url2.endsWith("/")) url2 += "/";
					v1 = new MappingResolver.MappingHandler(method, url2, server, new HashSet<>());
				} else {
					v1.setMethod(method);
				}
				v1.getRequestMethods().add(requestMethod);
				return v1;
			});
		}
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", MappingConfigurationImpl.class.getSimpleName() + "[", "]").add("mappingResolver=" + mappingResolver).toString();
	}
}
