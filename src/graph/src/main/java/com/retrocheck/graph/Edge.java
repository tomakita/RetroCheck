package com.retrocheck.graph;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public class Edge<U, V> {
    private Node<U> u;
    private Node<V> v;
    // NOTE: `refinement` must assign u's values to v, and not the other way around.  otherwise, u's values could be
    //       overwritten (and thus its constraints with other nodes broken) during the traversal.
    private BiFunction<U, V, V> refinement;
    // TODO: make this not optional -- just a normal string for which null is the default value.
    private Optional<String> setId = Optional.empty(); // None if not multiedge...maybe just have an explicit isMultiedge property?
    private int probability;
    private boolean isMultiedge;
    private boolean isConstrained;

    public static final Optional<String> NULL_SET_ID = Optional.of("NULL");

    // edge with 100% probability
    public Edge(Node<U> u, Node<V> v, BiFunction<U, V, V> refinement) {
        this.u = u;
        this.v = v;
        if (refinement == null) {
            this.refinement = (_u, _v) -> _v;
        } else {
            this.refinement = refinement;
            isConstrained = true;
        }
        this.probability = Probability.ALWAYS.getValue();
    }

    // edge
    public Edge(Node<U> u, Node<V> v, BiFunction<U, V, V> refinement, Probability probability) {
        this.u = u;
        this.v = v;
        if (refinement == null) {
            this.refinement = (_u, _v) -> _v;
        } else {
            this.refinement = refinement;
            isConstrained = true;
        }
        this.probability = probability.getValue();
    }

    // multiedge
    public Edge(Node<U> u, Node<V> v, BiFunction<U, V, V> refinement, Probability probability, String setId) {
        if (setId.equals(NULL_SET_ID.get())) {
            throw new IllegalArgumentException("setId cannot be 'NULL', as 'NULL' is a reserved value.");
        } else if (setId == null) {
            throw new IllegalArgumentException("setId cannot be null.");
        }

        this.u = u;
        this.v = v;
        if (refinement == null) {
            this.refinement = (_u, _v) -> _v;
        } else {
            this.refinement = refinement;
            isConstrained = true;
        }
        this.probability = probability.getValue();
        this.setId = Optional.of(setId);
        isMultiedge = true;
    }

    // only for internal use
    Edge(Node<U> u, Node<V> v, BiFunction<U, V, V> refinement, Probability probability, Optional<String> setId) {
        this.u = u;
        this.v = v;
        if (refinement == null) {
            this.refinement = (_u, _v) -> _v;
        } else {
            this.refinement = refinement;
            isConstrained = true;
        }
        this.probability = probability.getValue();
        this.setId = setId;
        if (!setId.equals(NULL_SET_ID) && setId.isPresent()) {
            isMultiedge = true;
        }
    }

    public void refine() {
        //V throwawayForNowAssumeRefinementMutatesItsLeftArg = refinement.apply(id.getFirst().getEntityInstance(), id.getSecond().getEntityInstance());
        v.setEntityInstance(refinement.apply(u.getEntityInstance(), v.getEntityInstance()));
    }

    public boolean isConstrained() {
        return isConstrained;
    }

    public int probability() {
        return probability;
    }

    public Optional<String> containingSetId() {
        return setId;
    }

    public Edge<U, V> copy(Map<UUID, Node<?>> nodesByIdCopy) {
        BiFunction<U, V, V> refinementCopy = this.isConstrained ? this.refinement : null;
        return new Edge((Node<U>)nodesByIdCopy.get(this.u.identify()), (Node<V>)nodesByIdCopy.get(v.identify()), refinementCopy, new Probability(this.probability), this.setId);
    }

    public Edge<U, V> deepCopy() {
        BiFunction<U, V, V> refinementCopy = this.isConstrained ? this.refinement : null;
        return new Edge(this.u.copyWithNewId(), this.v.copyWithNewId(), refinementCopy, new Probability(this.probability), this.setId);
    }

    public Edge<U, Object> terminate(int terminationProbability) {
        setId = setId.isPresent() ? setId : Optional.of(UUID.randomUUID().toString());
        isMultiedge = true;

        return new Edge<>(this.u, Node.getNull(), (u, v) -> v, new Probability(terminationProbability), setId);
    }

    public Tuple<String, String> getEndpointNames() {
        return new Tuple<>(this.u.toString(), this.v.toString());
    }

    public Node<U> getLeftEndpoint() {
        return this.u;
    }

    public Node<V> getRightEndpoint() {
        return this.v;
    }

    public Tuple<UUID, UUID> identify() {
        return new Tuple<>(this.u.identify(), this.v.identify());
    }

    public boolean isMultiedge() {
        return isMultiedge;
    }

    public String toString() {
        return "(" + this.u.toString() + ", " + this.v.toString() + ")";
    }
}
