package gr.uoa.di.rdf.Geographica3.runtime.querysets.partofworkload;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.IQuerySet;
import gr.uoa.di.rdf.Geographica3.runtime.querysets.partofworkload.impl.SimpleQSPartOfWorkload;

/**
 * An interface that represents a dependent Query Set as part of a workload.
 * Operations can be limited on a subpart of the queryset by using the 
 * {@link FilterAction} which allows either to include or exclude a list of
 * query numbers.
 * It is dependent because it is binded to a {@link IExecutionSpec}.
 * 
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */

/* The following @JsonDeserialize annotation HAD to be disabled because
  a) the default implementing class, SimpleQSPartOfWorkload, is DEPENDENT on
  the existence of the container SimpleGeospatialWL and b) because the data
  member IQuerySetPartOfWorkload geospatialQueryset of the container SimpleGeospatialWL
  is polymorphic, therefore a @JsonTypeInfo annotation was placed on the data
  member, which allows dynamic deserialization.
    
//@JsonDeserialize(as = SimpleQSPartOfWorkload.class)
*/
public interface IQuerySetPartOfWorkload extends IQuerySet {

    IExecutionSpec getExecutionSpec();

    void setExecutionSpec(IExecutionSpec executionSpec);


}
