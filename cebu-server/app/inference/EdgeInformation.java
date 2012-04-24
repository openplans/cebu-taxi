package inference;

import org.opentripplanner.routing.graph.Edge;

import gov.sandia.cognition.statistics.bayesian.conjugate.UnivariateGaussianMeanVarianceBayesianEstimator;
import gov.sandia.cognition.statistics.distribution.NormalInverseGammaDistribution;

public class EdgeInformation {

  private final NormalInverseGammaDistribution velocityPrecisionDist = 
      new NormalInverseGammaDistribution(0d, 1/Math.sqrt(30d), 6d/5d, 6d);
  
  private final UnivariateGaussianMeanVarianceBayesianEstimator velocityEstimator = 
      new UnivariateGaussianMeanVarianceBayesianEstimator(velocityPrecisionDist);
  
  private final Edge edge;

  public EdgeInformation(Edge edge) {
    super();
    this.edge = edge;
  }
  
  
  public void updateVelocity(double varianceDist) {
    /*
     * TODO should have a gamma for "observed" variance 
     */
    velocityEstimator.update(velocityPrecisionDist, varianceDist);
  }


  public NormalInverseGammaDistribution getVelocityPrecisionDist() {
    return velocityPrecisionDist;
  }


  public UnivariateGaussianMeanVarianceBayesianEstimator getVelocityEstimator() {
    return velocityEstimator;
  }


  public Edge getEdge() {
    return edge;
  }
  
}
