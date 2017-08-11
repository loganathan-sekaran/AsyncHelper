package org.ls.asynchelper;

import java.util.Arrays;
import java.util.List;

/**
 * The Class ObjectsKey.
 */
class ObjectsKey {

	/** The keys. */
	private List<Object> keys;
	
	/**
	 * Instantiates a new objects key.
	 *
	 * @param keys the keys
	 */
	private ObjectsKey(Object[] keys) {
		this.keys = Arrays.asList(keys);

	}

	/**
	 * Of.
	 *
	 * @param keys the keys
	 * @return the objects key
	 */
	public static ObjectsKey of(Object... keys) {
		assert(keys != null);
		assert(keys.length > 0);
		for (Object key : keys) {
			assert(key != null);
		}
		return new ObjectsKey(keys);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (Object obj : keys) {
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectsKey other = (ObjectsKey) obj;
		return this.keys.containsAll(other.keys) && other.keys.containsAll(this.keys);

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ObjectsKey [keys=" + keys + "]";
	}

}