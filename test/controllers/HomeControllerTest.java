package controllers;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class HomeControllerTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }

    @Test
    public void testIndex() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        Result result = route(app, request);
        assertEquals(OK, result.status());
    }
    
    @Test
    public void testFinances() {
        Http.RequestBuilder request = new Http.RequestBuilder()
            .method(GET)
            .uri("/finances/11");

        Result result = route(app, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void testSearch() {
        Http.RequestBuilder request = new Http.RequestBuilder()
            .method(GET)
            .uri("/api/search");

        Result result = route(app, request);
        System.out.println(result);
        assertEquals(OK, result.status());
    }

    /**
     * Tests the /movie/{id} endpoint.
     * Ensures that requesting a movie by ID returns HTTP 200 OK.
     */
    @Test
    public void testMovieDetailsEndpoint() {
        Result result = route(app,
                fakeRequest(GET, "/movie/11")
        );
        assertEquals(OK, result.status());
    }

    /**
     * Tests the /tv/{id} endpoint.
     * Ensures that requesting a TV show by ID returns HTTP 200 OK.
     */
    @Test
    public void testTvShowDetailsEndpoint() {
        Result result = route(app,
                fakeRequest(GET, "/tv/10")
        );
        assertEquals(OK, result.status());
    }

}
