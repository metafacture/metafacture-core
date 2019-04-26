package org.metafacture.metamorph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.metamorph.Registry;
import org.metafacture.metamorph.WildcardRegistry;
import org.metafacture.metamorph.api.Maps;
import org.metafacture.metamorph.api.NamedValuePipe;
import org.metafacture.metamorph.api.NamedValueReceiver;
import org.metafacture.metamorph.api.NamedValueSource;
import org.metafacture.metamorph.api.SourceLocation;

public class Metafix implements StreamPipe<StreamReceiver>, NamedValuePipe, Maps {

	public static final String ELSE_KEYWORD = "_else";
	private final List<NamedValueReceiver> elseSources = new ArrayList<>();
	private final Registry<NamedValueReceiver> dataRegistry = new WildcardRegistry<>();

	@Override
	public void startRecord(String identifier) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endRecord() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startEntity(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endEntity() {
		// TODO Auto-generated method stub

	}

	@Override
	public void literal(String name, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetStream() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeStream() {
		// TODO Auto-generated method stub

	}

	@Override
	public <R extends StreamReceiver> R setReceiver(R receiver) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void receive(String name, String value, NamedValueSource source, int recordCount, int entityCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addNamedValueSource(NamedValueSource namedValueSource) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSourceLocation(SourceLocation sourceLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public SourceLocation getSourceLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNamedValueReceiver(NamedValueReceiver receiver) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<String> getMapNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getMap(String mapName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue(String mapName, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> putMap(String mapName, Map<String, String> map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String putValue(String mapName, String key, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void registerNamedValueReceiver(final String source, final NamedValueReceiver data) {
		if (ELSE_KEYWORD.equals(source)) {
			elseSources.add(data);
		} else {
			dataRegistry.register(source, data);
		}
	}

}
