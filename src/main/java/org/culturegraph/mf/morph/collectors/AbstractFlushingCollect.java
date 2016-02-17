/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.morph.collectors;

import org.culturegraph.mf.morph.Metamorph;

import java.util.Map;

/**
 * Common basis for {@link Entity}, {@link Combine} etc.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public abstract class AbstractFlushingCollect extends AbstractCollect {

	public AbstractFlushingCollect(final Metamorph metamorph) {
		super(metamorph);
	}

	@Override
	public final void flush(final int recordCount, final int entityCount) {
		if ((isSameRecord(recordCount) && sameEntityConstraintSatisfied(entityCount) || (getIncludeSubEntities()
				&& isHierarchicalEntityEmitBufferFilled()))) {

			if (!getIncludeSubEntities()) {

				if (isConditionMet()) {

					emit();
				}
			} else {

				if (Combine.class.isInstance(this)) {

					((Combine) this).emitHierarchicalEntityBuffer();
				} else {

					emit();
				}
			}

			if (getReset()) {

				// to avoid condition reset, if not all conditions where satisfied + hierarchical entity end was not met
				if (!(getIncludeSubEntities() && All.class.isInstance(this) && !this.isComplete())) {

					if(All.class.isInstance(this)) {

						((All) this).clearLastMatchedEntity();
					} else {

						resetCondition();
						clear();
					}
				}
			}
		}

		if (getIncludeSubEntities()) {

			updateHierarchicalEntity(entityCount);

			// to avoid condition reset before hiearchical entity change
			if (!(All.class.isInstance(this) && !this.isComplete())) {

				setConditionMet(false);
			}

			if (getReset()) {

				// to avoid condition reset before hiearchical entity change
				if (!(All.class.isInstance(this) && !this.isComplete())) {

					if(All.class.isInstance(this)) {

						((All) this).clearLastMatchedEntity();
					} else {

						resetCondition();
						clear();
					}
				}
			}

			if (Combine.class.isInstance(this) && this.getConditionSource() != null && All.class.isInstance(this.getConditionSource())) {

				// force condition reset on hierarchical entity change

				((All) this.getConditionSource()).resetCondition();
				((All) this.getConditionSource()).clear();
				((All) this.getConditionSource()).updateHierarchicalEntity(entityCount);
				// ((All) this.getConditionSource()).setConditionMet(false);
				((All) this.getConditionSource()).forcedNonMatchedEmit();
			}
		}
	}

	protected void emitHierarchicalEntityBuffer() {

		for (final Map.Entry<String, String> emitEntry : getHierarchicalEntityBuffer()) {

			emit(emitEntry.getKey(), emitEntry.getValue());
		}
	}

	protected void emit(String name, String value) {

		getNamedValueReceiver().receive(name, value, this, getRecordCount(), getEntityCount());
	}
}
