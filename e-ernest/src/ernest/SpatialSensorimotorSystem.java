package ernest;

import javax.vecmath.Vector3f;

import imos.IAct;

import org.w3c.dom.Element;

import spas.IObservation;
import spas.IStimulation;
import spas.LocalSpaceMemory;
import spas.Observation;
import spas.Stimulation;

/**
 * Implement Ernest 10.0's sensorimotor system.
 * Ernest 10.0 has a visual resolution of 2x12 pixels and a kinesthetic resolution of 3x3 pixels.
 * @author ogeorgeon
 */
public class SpatialSensorimotorSystem  extends BinarySensorymotorSystem
{
	/** The current observation generated by the spatial system */
	private IObservation m_observation = null;
	
	private String m_visualStimuli = "";
	private String m_stimuli = "";
	private int m_satisfaction = 0;
	private boolean m_status;
	private IAct m_primitiveAct = null;

	final static float TRANSLATION_IMPULSION = .15f; // .13f
	final static float ROTATION_IMPULSION = 0.123f;//(float)Math.toRadians(7f); // degrees   . 5.5f

	public int[] update(int[][] stimuli) 
	{
		int primitiveSchema[] = new int[2];
		float translationx = (float) stimuli[2][8] / Ernest.INT_FACTOR; 
		float translationy = (float) stimuli[3][8] / Ernest.INT_FACTOR;
		float rotation = (float) stimuli[4][8] / Ernest.INT_FACTOR;
		float speed = (float) stimuli[5][8] / Ernest.INT_FACTOR;
		int cognitiveMode = stimuli[6][8];

		// Update the local space memory
    	m_spas.update(new Vector3f(-translationx, -translationy, 0), - rotation);

    	sense(stimuli);
    	primitiveSchema[0] = 0;
    	primitiveSchema[1] = 0;

    	// Trigger a new cognitive loop when the speed is below a threshold.
        if ((speed <= .050f) && (Math.abs(rotation) <= .050f) && cognitiveMode > 0)
        {
    		IAct enactedPrimitiveAct = null;
 
    		// If the intended act was null (during the first cycle), then the enacted act is null.

    		// Compute the enacted act == 
    		
    		if (m_primitiveAct != null)
    		{
    			if (m_primitiveAct.getSchema().getLabel().equals(">"))
    				m_satisfaction = m_satisfaction + (m_status ? 20 : -100);
    			else
    				m_satisfaction = m_satisfaction + (m_status ? -10 : -20);
    		
    			enactedPrimitiveAct = m_imos.addInteraction(m_primitiveAct.getSchema().getLabel(), m_stimuli, m_satisfaction);

    			if (m_tracer != null) 
    				m_tracer.addEventElement("primitive_enacted_schema", m_primitiveAct.getSchema().getLabel());
    		}
    		
    		// Let Ernest decide for the next primitive schema to enact.
    		
    		m_primitiveAct = m_imos.step(enactedPrimitiveAct);
    		primitiveSchema[0] = m_primitiveAct.getSchema().getLabel().toCharArray()[0];
    		
    		// Once the decision is made, compute the intensity.
    		
    		primitiveSchema[1] = impulsion(primitiveSchema[0]);
        }

		return primitiveSchema;    		
	}
	public IAct enactedAct(IAct act, int[][] stimuli) 
	{
		// If the intended act was null (during the first cycle), then the enacted act is null.
		IAct enactedAct = null;		

		// Sense the environment ===
		
		sense(stimuli);
		
		// Compute the enacted act == 
		
		if (act != null)
		{
			if (act.getSchema().getLabel().equals(">"))
				m_satisfaction = m_satisfaction + (m_status ? 20 : -100);
			else
				m_satisfaction = m_satisfaction + (m_status ? -10 : -20);
		
			enactedAct = m_imos.addInteraction(act.getSchema().getLabel(), m_stimuli, m_satisfaction);

			if (m_tracer != null) 
				m_tracer.addEventElement("primitive_enacted_schema", act.getSchema().getLabel());
		}
		return enactedAct;
	}
	
	public void sense(int[][] stimuli)
	{
		// Vision =====
		
		IStimulation[] visualStimulations = new Stimulation[Ernest.RESOLUTION_RETINA];
		for (int i = 0; i < Ernest.RESOLUTION_RETINA; i++)
		{
			visualStimulations[i] = m_spas.addStimulation(Ernest.MODALITY_VISUAL, stimuli[i][1] * 65536 + stimuli[i][2] * 256 + stimuli[i][3]);
			float angle = (float) (- 11 * Math.PI/24 + i * Math.PI/12); 
			Vector3f pos = new Vector3f((float) Math.cos(angle) * stimuli[i][0] / Ernest.INT_FACTOR, (float) Math.sin(angle) * stimuli[i][0] / Ernest.INT_FACTOR, 0);
			visualStimulations[i].setPosition(pos);
		}
		if (m_tracer != null) 
		{
			Object retinaElmt = m_tracer.addEventElement("retina");
			for (int i = Ernest.RESOLUTION_RETINA - 1; i >= 0 ; i--)
				m_tracer.addSubelement(retinaElmt, "pixel_0_" + i, visualStimulations[i].getHexColor());
		}
		
		// Touch =====
		
		IStimulation [] tactileStimulations = new IStimulation[9];
		
		for (int i = 0; i < 9; i++)
		{
			tactileStimulations[i] = m_spas.addStimulation(Ernest.MODALITY_TACTILE, stimuli[i][9]);
		}
			
		if (m_tracer != null)
		{
			Object s = m_tracer.addEventElement("tactile");
			m_tracer.addSubelement(s, "here", tactileStimulations[8].getHexColor());
			m_tracer.addSubelement(s, "rear", tactileStimulations[7].getHexColor());
			m_tracer.addSubelement(s, "touch_6", tactileStimulations[6].getHexColor());
			m_tracer.addSubelement(s, "touch_5", tactileStimulations[5].getHexColor());
			m_tracer.addSubelement(s, "touch_4", tactileStimulations[4].getHexColor());
			m_tracer.addSubelement(s, "touch_3", tactileStimulations[3].getHexColor());
			m_tracer.addSubelement(s, "touch_2", tactileStimulations[2].getHexColor());
			m_tracer.addSubelement(s, "touch_1", tactileStimulations[1].getHexColor());
			m_tracer.addSubelement(s, "touch_0", tactileStimulations[0].getHexColor());
		}
			
		// Kinematic ====
		
		IStimulation kinematicStimulation;
		
		kinematicStimulation = m_spas.addStimulation(Ernest.STIMULATION_KINEMATIC, stimuli[1][8]);

		// Taste =====
		
		IStimulation gustatoryStimulation = m_spas.addStimulation(Ernest.STIMULATION_GUSTATORY, stimuli[0][8]); 

		// Process the spatial implications of the enacted interaction ====
		
		IObservation newObservation = m_spas.step(visualStimulations, tactileStimulations, kinematicStimulation, gustatoryStimulation);		

		if (m_observation != null)
			setDynamicFeature(m_observation, newObservation);

		m_observation = newObservation;
		
		if (m_tracer != null) 
		{
			Object e = m_tracer.addEventElement("current_observation");
			m_tracer.addSubelement(e, "direction", newObservation.getDirection() + "");
			m_tracer.addSubelement(e, "distance", newObservation.getDistance() + "");
			m_tracer.addSubelement(e, "span", newObservation.getSpan() + "");
			m_tracer.addSubelement(e, "attractiveness", newObservation.getAttractiveness() + "");
			m_tracer.addSubelement(e, "stimuli", m_stimuli);
			m_tracer.addSubelement(e, "dynamic_feature", m_visualStimuli);
			m_tracer.addSubelement(e, "satisfaction", m_satisfaction + "");
			m_tracer.addSubelement(e, "kinematic", newObservation.getKinematicStimulation().getHexColor());
			m_tracer.addSubelement(e, "gustatory", newObservation.getGustatoryStimulation().getHexColor());
		}

		m_observation = newObservation;
	}
	
	/**
	 * Generate the dynamic stimuli from the impact in the local space memory.
	 * The stimuli come from: 
	 * - The kinematic feature.
	 * - The variation in attractiveness and in direction of the object of interest. 
	 * @param act The enacted act.
	 */
	private void setDynamicFeature(IObservation previousObservation, IObservation newObservation)
	{
		int   newAttractiveness = newObservation.getAttractiveness();
		float newDirection = newObservation.getDirection();
		int   previousAttractiveness = previousObservation.getAttractiveness();
		float previousDirection = previousObservation.getDirection();
		
		Vector3f relativeSpeed = new Vector3f(newObservation.getPosition());
		relativeSpeed.sub(previousObservation.getPosition());
		
		String dynamicFeature = "";
		
		float minFovea =  - (float)Math.PI / 4 + 0.01f;
		float maxFovea =    (float)Math.PI / 4 - 0.01f;
		
		int satisfaction = 0;

		if (newAttractiveness >= 0)
		{
			// Positive attractiveness
			{
				// Attractiveness
				if (previousAttractiveness > newAttractiveness)
					// Farther
					dynamicFeature = "-";
				else if (previousAttractiveness < newAttractiveness)
					// Closer
					dynamicFeature = "+";
				//else if (Math.abs(previousDirection) < Math.abs(newDirection))
				else if (relativeSpeed.y * newDirection > 0)
					// More outward (or same direction, therefore another salience)
					dynamicFeature = "-";
				//else if (Math.abs(previousDirection) > Math.abs(newDirection))
				else if (relativeSpeed.y * newDirection < 0)
					// More inward
					dynamicFeature = "+";
		
				if (dynamicFeature.equals("-"))
					satisfaction = -100;
				if (dynamicFeature.equals("+"))
					satisfaction = 20;
	
				// Direction
				
				if (!dynamicFeature.equals(""))
				{
					if (newDirection <= minFovea)
						dynamicFeature = "|" + dynamicFeature;
					else if (newDirection >= maxFovea )
						dynamicFeature = dynamicFeature + "|";
				}		
			}
		}
		else
		{
			// Negative attractiveness (repulsion)
			
			// Variation in attractiveness
			if (previousAttractiveness >= 0)
				// A wall appeared with a part of it in front of Ernest
				dynamicFeature = "*";		
			//else if (Math.abs(previousDirection) < Math.abs(newDirection))
			else if (relativeSpeed.y * newDirection > 0)
				// The wall went more outward (Ernest closer to the edge)
				dynamicFeature = "_";
			//else if (Math.abs(previousDirection) > Math.abs(newDirection))
			else if (relativeSpeed.y * newDirection < 0)
				// The wall went more inward (Ernest farther to the edge)
				dynamicFeature = "*";
	
			if (dynamicFeature.equals("*"))
				satisfaction = -100;
			if (dynamicFeature.equals("_"))
				satisfaction = 20;
			
			// Direction feature
			
			if (!dynamicFeature.equals(""))
			{
				if (newDirection < -0.1f ) 
					dynamicFeature = "|" + dynamicFeature;
				else if (newDirection > 0.1f )
					dynamicFeature = dynamicFeature + "|";
			}		
		}
		
		// Gustatory
		
		if (newObservation.getGustatoryStimulation().getValue() != Ernest.STIMULATION_GUSTATORY_NOTHING)
		{
			dynamicFeature = "e";
			satisfaction = 100;
		}
		
		m_visualStimuli = dynamicFeature;
		
		// Kinematic
		
		m_status = (newObservation.getKinematicStimulation().getValue() != Ernest.STIMULATION_KINEMATIC_BUMP);
		
		m_stimuli = (m_status ? " " : "w") + dynamicFeature;

		m_satisfaction = satisfaction;
	}
	
	public int impulsion(int intentionSchema) 
	{
		int impulsion = 0;
		
		if (intentionSchema == '>')
		{
			impulsion = (int)(TRANSLATION_IMPULSION * Ernest.INT_FACTOR);
			if (m_observation.getDistance() < .5f)
				impulsion = (int)(TRANSLATION_IMPULSION * Ernest.INT_FACTOR * .5f);
			if (m_observation.getDistance() < 1.1f)
				impulsion = (int)(TRANSLATION_IMPULSION * Ernest.INT_FACTOR * m_observation.getDistance());
			else
				impulsion = (int)(TRANSLATION_IMPULSION * Ernest.INT_FACTOR * 1.1f);
		}
		if (intentionSchema == '^' || intentionSchema == 'v' )
		{ 
			impulsion = (int)(ROTATION_IMPULSION * Ernest.INT_FACTOR);
			if (Math.abs(m_observation.getDirection()) > Math.PI/8)
				impulsion = (int)(ROTATION_IMPULSION * Ernest.INT_FACTOR * Math.abs(m_observation.getDirection()) / (Math.PI/4));
			if (impulsion > 2 * ROTATION_IMPULSION * Ernest.INT_FACTOR)
				impulsion = (int)(2 * ROTATION_IMPULSION * Ernest.INT_FACTOR);
			
			//return (int)(Math.abs(m_observation.getPlace().getDirection()) * 1000);
		}
		return impulsion;
	}
}