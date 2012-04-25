package controllers;

import java.io.File;

import com.google.inject.Inject;

import akka.actor.ActorRef;
import akka.actor.Props;
import async.CsvUploadActor;

import play.*;
import play.libs.Akka;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import views.html.*;

public class Application extends Controller {
  
  public static Result index() {
    return ok(index.render());
  }
  
  public static Result upload(	) {
	
	  return ok(upload.render());
  }
  
  public static Result uploadHandler() {
	  MultipartFormData body = request().body().asMultipartFormData();
	  FilePart csv = body.getFile("csv");
	  
	  if (csv != null) {
	 
	    File csvFile = csv.getFile();
	    
	    ActorRef csvActor = Akka.system().actorOf(new Props(CsvUploadActor.class));
	    
	    csvActor.tell(csvFile);
	    
	    return ok("File uploaded");
	  }
	 
	  return redirect(routes.Application.upload());     
  }  
  

 
  
}