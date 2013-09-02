package eca.construct;

import eca.spas.egomem.Place;

/**
 * An instance of phenomenon known to be present in the surrounding environment
 * @author Olivier
 */
public class PhenomenonInstanceImpl implements PhenomenonInstance {

	private PhenomenonType phenomenonType = null;
	private Place place = null;
	
	public PhenomenonInstanceImpl(PhenomenonType phenomenonType, Place place){
		this.phenomenonType = phenomenonType;
		this.place = place;
	}
	
	public PhenomenonType getPhenomenonType() {
		return this.phenomenonType;
	}

	public Place getPlace() {
		return this.place;
	}

	public void setPhenomenonType(PhenomenonType phenomenonType) {
		this.phenomenonType = phenomenonType;
	}
	
	public String toString(){
		return ("Type " + this.phenomenonType.getLabel() + " in area " + place.getArea().getLabel()); 
	}

}
