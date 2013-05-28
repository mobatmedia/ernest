package imos2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import spas.Area;
import spas.SimuImpl;
import ernest.Action;
import ernest.ActionImpl;
import ernest.Aspect;
import ernest.AspectImpl;
import ernest.Observation;
import ernest.ObservationImpl;
import ernest.Primitive;
import ernest.PrimitiveImpl;

/**
 * A sensorimotor pattern of interaction of Ernest with its environment 
 * @author Olivier
 */
public class ActImpl implements Act 
{

	/** The list of all acts */
	private static Map<String , Act> ACTS = new HashMap<String , Act>() ;

	/** Default weight of primitive interactions */
	private static int PRIMITIVE_WEIGHT = 100;
	
	private String label = "";
	private boolean m_primitive = true;
	private Act m_preInteraction = null;
	private Act m_postInteraction = null;
	private int value = 0;
	private int m_enactionWeight = 0;
	private int m_length = 1;
	private Act m_prescriber = null;
	private int m_step = 0;
	private Primitive primitive;
	private Action action;
	//private Observation observation;
	private Area area;
	
	/**
	 * @return The list of all acts known by the agent.
	 */
	public static Collection<Act> getACTS(){
		return ACTS.values();
	}

	/**
	 * @param interaction The primitive interaction from which this act is made.
	 * @param area The area.
	 * @return The created primitive interaction.
	 */
	public static Act createOrGetPrimitiveAct(Primitive interaction, Area area)
	{
		String key = createPrimitiveKey(interaction, area);
		if (!ACTS.containsKey(key)){
			ActImpl newAct = new ActImpl(key, true, null, null, interaction.getValue(), interaction, area);
//			newAct.setAction(ActionImpl.createNew());
			ACTS.put(key, newAct);

//			if (area.equals(SimuImpl.O))
//				interaction.setAspect(SimuImpl.EMPTY); 
//			else 
//				interaction.setAspect(SimuImpl.ANYTHING); 
//			
			System.out.println("Define primitive act " + key);
//			System.out.println("With action " + newAct.getAction().getLabel());
		}
		return ACTS.get(key);
	}
	
	private static String createPrimitiveKey(Primitive interaction, Area area) {
		String key = interaction.getLabel() + area.getLabel();
		return key;
	}
	
	/**
	 * @param preAct The pre-act.
	 * @param postAct The post-act.
	 * @return The created composite interaction.
	 */
	public static Act createOrGetCompositeAct(Act preAct, Act postAct)
	{
		String key = createCompositeKey(preAct, postAct);
		int enactionValue = preAct.getEnactionValue() + postAct.getEnactionValue();
		if (!ACTS.containsKey(key))
			ACTS.put(key, new ActImpl(key, false, preAct, postAct, enactionValue, null, null));			
		return ACTS.get(key);
		//String label = preInteraction.getLabel() + postInteraction.getLabel();
		//return new ActImpl(label, false, preInteraction, postInteraction, enactionValue, null, null);
	}
	
	public static Act getAct(Action action, Observation observation){
		
		Primitive interaction = action.getPrimitives().get(0);
		for (Primitive i : PrimitiveImpl.getINTERACTIONS()){
			if (i.getAction().equals(action) && i.getAspect().equals(observation.getAspect()))
				interaction = i;
		}

		Act act = createOrGetPrimitiveAct(interaction, observation.getArea());
		
//		for (Act a : ACTS.values()){
//			//if (action.equals(a.getAction()) && observation.equals(a.getObservation()))
//			if (action.equals(a.getAction()) && a.getPrimitive().getAspect().equals(observation.getAspect()) && a.getArea().equals(observation.getArea()))			
//				return a;
//		}
		return act;
	}
	
	private static String createCompositeKey(Act preAct, Act postAct) {
		String key = preAct.getLabel() + postAct.getLabel();
		return key;
	}
	
	private ActImpl(String label, boolean primitive, Act preInteraction, Act postInteraction, int value, Primitive interaction, Area area)
	{
		this.label = label;
		m_primitive = primitive;
		this.primitive = interaction;
		m_preInteraction = preInteraction;
		m_postInteraction = postInteraction;
		this.value = value;
		if (primitive)
			m_enactionWeight = PRIMITIVE_WEIGHT;
		else
			m_length = preInteraction.getLength() + postInteraction.getLength();
		//this.interaction = interaction;
		this.area = area;
	}
	
	public Action getAction() {
		System.out.println(label);
		return this.primitive.getAction();
	}
//	
//	public void setAction(Action action){
//		this.action = action;
//		action.addAct(this);
//	}

//	public Observation getObservation() {
//		return this.observation;
//	}
//	
//	public void setObservation(Observation observation){
//		this.observation = observation;
//		observation.addAct(this);
//	}

	public Act getPreAct() 
	{
		return m_preInteraction;
	}

	public Act getPostAct() 
	{
		return m_postInteraction;
	}

	public int getEnactionValue() 
	{
		return this.value;
	}

	public boolean isPrimitive() 
	{
		return m_primitive;
	}
	
	/**
	 * Interactions are equal if they have the same label. 
	 */
	public boolean equals(Object o)
	{
		boolean ret = false;
		
		if (o == this)
			ret = true;
		else if (o == null)
			ret = false;
		else if (!o.getClass().equals(this.getClass()))
			ret = false;
		else
		{
			Act other = (Act)o;
			ret = (other.getLabel().equals(getLabel()));
		}
		
		return ret;
	}

	public String getLabel() 
	{
		String l = "";
		if (m_primitive)
			l = this.label;
		else
			l = "(" + m_preInteraction.getLabel() + m_postInteraction.getLabel() + ")";
		return l; 
	}

	public void setWeight(int enactionWeight) 
	{
		m_enactionWeight = enactionWeight;
	}

	public int getWeight() 
	{
		return m_enactionWeight;
	}

	public int getLength() 
	{
		return m_length;
	}

	public void setStep(int step) 
	{
		m_step = step;
	}

	public int getStep() 
	{
		return m_step;
	}

	public void setPrescriber(Act prescriber) 
	{
		m_prescriber = prescriber;
	}

	public Act getPrescriber() 
	{
		return m_prescriber;
	}
	
	/**
	 * Update the prescriber if this interaction was enacted
	 */
	public Act updatePrescriber()
	{
		Act prescriber = m_prescriber;
		m_prescriber = null;
		Act nextInteraction = null;
		if (prescriber != null)
		{
			int step = prescriber.getStep();
			if (step == 0)
			{
				// The prescriber's pre-interaction was enacted
				prescriber.setStep(step + 1);
				nextInteraction = prescriber.getPostAct();
				nextInteraction.setPrescriber(prescriber);
			}
			else
			{
				// The prescriber's post-interaction was enacted
				// Update the prescriber's prescriber
				nextInteraction = prescriber.updatePrescriber();
			}
		}
		
		return nextInteraction;
	}

	public void terminate()
	{
		if (m_prescriber != null)
		{
			m_prescriber.terminate();
			m_prescriber = null;
		}
		m_step = 0;
	}

	public Act prescribe() 
	{
		Act prescribedInteraction = null;
		if (m_primitive)
			prescribedInteraction = this;
		else
		{
			m_step = 0;
			m_preInteraction.setPrescriber(this);
			prescribedInteraction = m_preInteraction.prescribe();
		}
		return prescribedInteraction;		
	}
	
	public String toString()
	{
		return getLabel() + "(" + value/10 + "," + m_enactionWeight + ")";
	}

//	public Aspect getAspect() {
//		return this.aspect;
//	}
//
//	public void setAspect(Aspect aspect) {
//		this.aspect = aspect;
//		aspect.addAct(this);
//	}

	public Area getArea() {
		return this.area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public Primitive getPrimitive() {
		return this.primitive;
	}

	public void setPrimitive(Primitive primitive) {
		this.primitive = primitive;
	}
}