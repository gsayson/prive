package tests.broskiclan.prive;

import dev.priveweb.core.http.request.verbs.GetRequest;
import dev.priveweb.core.http.request.QueryParameters;

public class TestListener {

	@GetRequest("/wow")
	public void test(QueryParameters queryParameters) {
		System.out.println("Query parameters test got " + queryParameters);
	}

	@GetRequest("/nav")
	public void test2() {
		System.out.println("Hello, from test listener!");
	}

}
