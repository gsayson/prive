module dev.priveweb.prive {

	requires org.jetbrains.annotations;
	requires lombok;
	requires org.slf4j;
	requires moshi;
	requires info.picocli;
	requires trove4j;
	requires slf4jansi;
	requires org.fusesource.jansi;
	requires jdk.unsupported;

	exports dev.priveweb.core.exposure to tests.broskiclan.prive;
	exports dev.priveweb.core.util to tests.broskiclan.prive;

	exports dev.priveweb.core.http;
	exports dev.priveweb.core.mapper;
	exports dev.priveweb.core.http.request;
	exports dev.priveweb.core.http.handlers;
	exports dev.priveweb.core.protocol;
	exports dev.priveweb.core.http.response;
	exports dev.priveweb.core.http.interceptor;
	exports dev.priveweb.core.marshal;
	exports dev.priveweb.core.server;
	exports dev.priveweb.core.server.impl;
	exports dev.priveweb.core.exception;
	exports dev.priveweb.core.data.impl;
	exports dev.priveweb.core.data;
	exports dev.priveweb.core;
	exports dev.priveweb.core.http.session;
	exports dev.priveweb.core.http.request.verbs;

}