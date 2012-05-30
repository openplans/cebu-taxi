package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import jobs.ObservationHandler;

import models.*;

public class Application extends Controller {

	public static void index() {
		render();
	}
	
    public static void recent() {
    	Queue history = ObservationHandler.historyQueue;
    	response.setContentTypeIfNotSet("text/plain");
        render(history);
    }

}