/*
 * Copyright (c) 2011, Marc Röttig.
 *
 * This file is part of GenericKnimeNodes.
 * 
 * GenericKnimeNodes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ballproject.knime.base.parameter;

import java.io.Serializable;

/**
 * The generic Parameter base class is used to store all possible CTD parameters (double, int, string, int list, ...).
 * 
 * @author roettig
 *
 * @param <T>
 */
public abstract class Parameter<T> implements Serializable
{
	protected String  key;
	protected T       value;
	protected String  description = "";
	protected String  section     = "default";
	protected boolean isOptional = true;
	protected boolean advanced   = false;
	
	/**
	 * ctor with unique key of parameter and generic value to store.
	 * 
	 * @param key the key of the parameter
	 * @param value the generic value of the parameter
	 */
	public Parameter(String key, T value)
	{
		this.key   = key;
		this.value = value;
	}

	/**
	 * returns the associated unique key (name) of the parameter.
	 * 
	 * @return key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * sets the unique key (name) of the parameter.
	 * 
	 * @param key the key of the parameter
	 */
	public void setKey(String key)
	{
		this.key = key;
	}

	/**
	 * returns the generic value stored by this object.
	 * @return
	 */
	public T getValue()
	{
		return value;
	}

	/**
	 * sets the value stored by this object.
	 * 
	 * @param value the value to store
	 */
	public void setValue(T value)
	{
		this.value = value;
	}

	/**
	 * returns the description for this parameter object.
	 * 
	 * @return description the descriptional text of the parameter
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * sets the description of this parameter object.
	 * 
	 * @param description the descriptional text of the parameter
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * returns the section (category) for the parameter.
	 * 
	 * @return section the section of the parameter
	 */
	public String getSection()
	{
		return section;
	}

	/**
	 * sets the section (category) for the parameter.
	 * 
	 * @param section the section of the parameter
	 */
	public void setSection(String section)
	{
		this.section = section;
	}
	
	/**
	 * returns whether this parameter is initialized with a valid value or not.
	 * 
	 * @return is the parameter initialized
	 */
	public boolean isNull()
	{
		if(value==null)
			return true;
		return false;
	}
		
	/**
	 * returns whether the parameter is deemed optional.
	 * 
	 * @return is the parameter optional
	 */
	public boolean getIsOptional()
	{
		return isOptional;
	}

	/**
	 * sets whether the parameter is deemed optional.
	 * 
	 * @param isOptional flag whether parameter is optional
	 */
	public void setIsOptional(boolean isOptional)
	{
		this.isOptional = isOptional;
	}
	
	/**
	 * returns a textual information about the data type stored in this object.
	 * 
	 * @return mnemonic of parameter
	 */
	public abstract String getMnemonic();

	/**
	 * extracts data stored in the supplied string (previously generated by {@link Parameter#getStringRep()}) representation and set the value accordingly.
	 *  
	 * @param s special string representation of parameter
	 * @throws InvalidParameterValueException
	 */
	public abstract void fillFromString(String s) throws InvalidParameterValueException;
	
	/**
	 * returns a special string representation which can be xferred through string channels and reconstructed later on using {@link Parameter#fillFromString()}.
	 * 
	 * @return special string representation of parameter
	 */
	public String getStringRep()
	{
		return toString();
	}
	
	public boolean isAdvanced()
	{
		return advanced;
	}

	public void setAdvanced(boolean advanced)
	{
		this.advanced = advanced;
	}

	/**
	 * checks whether the supplied generic value is compatible with the data type of the parameter.
	 * 
	 * @param val value to validate
	 * 
	 * @return is the supplied value valid 
	 */
	public abstract boolean validate(T val);
	
	
	
	protected static String SEPERATORTOKEN = "@@@__@@@";
	
}
