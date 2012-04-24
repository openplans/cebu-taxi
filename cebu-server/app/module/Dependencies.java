package module;

import com.google.inject.*;
import utils.*;

public class Dependencies implements Module {

 public void configure(Binder binder) {
     binder.bind(OtpGraph.class).to(OtpGraphImpl.class);
  }  
}