package utils;

import java.util.List;
import java.util.Set;

import org.opentripplanner.routing.graph.Edge;

import com.google.common.base.Preconditions;

public class SnappedEdges {

  final private Set<Integer> snappedEdges;
  final private List<Edge> pathTraversed;

  public SnappedEdges(Set<Integer> snappedEdges, List<Edge> pathTraversed) {
    Preconditions.checkNotNull(snappedEdges);
    Preconditions.checkNotNull(pathTraversed);
    this.snappedEdges = snappedEdges;
    this.pathTraversed = pathTraversed;
  }

  public Set<Integer> getSnappedEdges() {
    return snappedEdges;
  }

  public List<Edge> getPathTraversed() {
    return pathTraversed;
  }

}
