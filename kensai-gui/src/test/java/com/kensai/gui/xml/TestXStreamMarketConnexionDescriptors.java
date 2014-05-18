package com.kensai.gui.xml;

import static com.kensai.gui.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class TestXStreamMarketConnexionDescriptors {

	private MarketConnectionDescriptor connexionSWX = new MarketConnectionDescriptor("SWX", "localhost", 1664, true);
	private MarketConnectionDescriptor connexionMIB = new MarketConnectionDescriptor("MIB", "192.168.0.255", 4661, false);
	private MarketConnexionDescriptors connexions = new MarketConnexionDescriptors(connexionSWX, connexionMIB);

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private File file;

	private XStream xstream;

	@Before
	public void before() throws IOException {
		file = folder.newFile("connexions.xml");

		xstream = new XStream(new StaxDriver());
		xstream.alias("MarketConnexionDescriptors", MarketConnexionDescriptors.class);
		xstream.alias("MarketConnexionDescriptor", MarketConnectionDescriptor.class);
	}

	@Test
	public void should_serialize_and_then_deserialize() throws IOException {
		// WHEN: serialize connexions into file
		Writer osWriter = new OutputStreamWriter(new FileOutputStream(file));
		PrettyPrintWriter ppWritter = new PrettyPrintWriter(osWriter);
		xstream.marshal(connexions, ppWritter);

		// AND: deserialize
		Reader isReader = new InputStreamReader(new FileInputStream((file)));
		MarketConnexionDescriptors fromXml = (MarketConnexionDescriptors) xstream.fromXML(isReader);

		// THEN: same object has been serialized/deserialized
		assertThat(fromXml).isNotNull().hasConnexions(connexions.getConnexions().toArray(new MarketConnectionDescriptor[] {}));
	}
}
