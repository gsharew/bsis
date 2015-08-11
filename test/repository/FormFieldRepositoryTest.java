package repository;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import model.admin.FormField;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test using DBUnit to test the FormFieldRepository
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:**/applicationContextTest.xml")
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@WebAppConfiguration
public class FormFieldRepositoryTest {
	
	@Autowired
	FormFieldRepository formFieldRepository;
	
	@Autowired
	private DataSource dataSource;
	
	private IDataSet getDataSet() throws Exception {
		File file = new File("test/dataset/FormFieldRepositoryDataset.xml");
		return new FlatXmlDataSetBuilder().setColumnSensing(true).build(file);
	}
	
	private IDatabaseConnection getConnection() throws SQLException {
		IDatabaseConnection connection = new DatabaseDataSourceConnection(dataSource);
		DatabaseConfig config = connection.getConfig();
		config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
		return connection;
	}
	
	@Before
	public void init() throws Exception {
		IDatabaseConnection connection = getConnection();
		try {
			IDataSet dataSet = getDataSet();
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
		}
		finally {
			connection.close();
		}
	}
	
	@After
	public void after() throws Exception {
		IDatabaseConnection connection = getConnection();
		try {
			IDataSet dataSet = getDataSet();
			DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
		}
		finally {
			connection.close();
		}
	}
	
	@Test
	@Transactional
	public void testGetAll() throws Exception {
		List<FormField> all = formFieldRepository.allFormFields();
		Assert.assertNotNull("There are FormFields", all);
		Assert.assertEquals("There are 170 FormFields", 170, all.size());
	}
	
	@Test
	@Transactional
	public void testFindFormFieldById() throws Exception {
		FormField one = formFieldRepository.findFormFieldById(1l);
		Assert.assertNotNull("There is a FormField with id 1", one);
		Assert.assertEquals("The Form field matches what was expected", "Donor", one.getForm());
	}
	
	@Test
	@Transactional
	public void testFindFormFieldByIdUnknown() throws Exception {
		FormField one = formFieldRepository.findFormFieldById(1111l);
		Assert.assertNull("There is no FormField with id 1111", one);
	}
	
	@Test
	@Transactional
	public void testGetFormField() throws Exception {
		FormField donorField = formFieldRepository.getFormField("Donor", "donorNumber");
		Assert.assertNotNull("There is a donorNumber field for the Donor form", donorField);
		Assert.assertEquals("The Form field matches what was expected", "Donor", donorField.getForm());
	}
	
	@Test
	@Transactional
	public void testGetFormFieldUnknown() throws Exception {
		FormField donorField = formFieldRepository.getFormField("Donor", "junit");
		Assert.assertNull("There is no junit field for the Donor form", donorField);
	}
	
	@Test
	@Transactional
	public void testGetAllDonorForm() throws Exception {
		List<FormField> all = formFieldRepository.getFormFields("Donor");
		Assert.assertNotNull("There are FormFields for the Donor Form", all);
		Assert.assertEquals("There are 46 FormFields", 46, all.size());
	}
	
	@Test
	@Transactional
	public void testGetRequiredFormFieldsDonorForm() throws Exception {
		List<String> all = formFieldRepository.getRequiredFormFields("Donor");
		Assert.assertNotNull("There are FormFields for the Donor Form", all);
		Assert.assertEquals("There are 5 required FormFields", 5, all.size());
		Assert.assertTrue("donorNumber is a required field for the Donor form", all.contains("donorNumber"));
	}
	
	@Test
	@Transactional
	public void testGetRequiredFormFieldsUnknownForm() throws Exception {
		List<String> all = formFieldRepository.getRequiredFormFields("Junit");
		Assert.assertNotNull("There are no FormFields for the unknown Form", all);
		Assert.assertEquals("There are no FormFields for the unknown Form", 0, all.size());
	}
	
	@Test
	@Transactional
	public void testGetFieldMaxLengths() throws Exception {
		Map<String, Integer> maxLengths = formFieldRepository.getFieldMaxLengths("Donor");
		Assert.assertNotNull("There is a non-null result", maxLengths);
		Assert.assertEquals("There are 45 FormFields for Donor Form", 45, maxLengths.size());
		// FIXME: there is a bug here - callingName field is repeated for Donor Form
		Integer length = maxLengths.get("donorNumber");
		Assert.assertEquals("donorNumber has a maxLength of 15", new Integer(15), length);
	}
	
	@Test
	@Transactional
	@Ignore("Because this test updates data and rollback is not working correctly, DBUnit hangs when cleaning up the database")
	public void testUpdateFormField() throws Exception {
		FormField donorField = formFieldRepository.getFormField("Donor", "donorNumber");
		Assert.assertNotNull("There is a donorNumber field for the Donor form", donorField);
		donorField.setMaxLength(123);
		formFieldRepository.updateFormField(donorField);
		FormField savedDonorField = formFieldRepository.getFormField("Donor", "donorNumber");
		Assert.assertEquals("The Form field matches what was expected", new Integer(123), savedDonorField.getMaxLength());
	}
}
