/**
 * Author: Lon Smith, Ph.D.
 * Description: This is the framework for the database program. Additional requirements and functionality
 *    are to be built by you and your group.
 */

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EmployeeSearchFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtDatabase;
	private JList<String> lstDepartment;
	private DefaultListModel<String> department = new DefaultListModel<String>();
	private JList<String> lstProject;
	private DefaultListModel<String> project = new DefaultListModel<String>();
	private JTextArea textAreaEmployee;
	private JCheckBox chckbxNotDept;
	private JCheckBox chckbxNotProject;
	
	// Database connection parameters
	private static final String DB_URL_PREFIX = "jdbc:mysql://localhost:3306/";
	private static final String DB_USER = "root";  // Change as needed
	private static final String DB_PASSWORD = "";  // Change as needed
	private Connection connection = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EmployeeSearchFrame frame = new EmployeeSearchFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public EmployeeSearchFrame() {
		setTitle("Employee Search");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 347);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Database:");
		lblNewLabel.setFont(new Font("Times New Roman", Font.BOLD, 12));
		lblNewLabel.setBounds(21, 23, 59, 14);
		contentPane.add(lblNewLabel);
		
	txtDatabase = new JTextField();
	txtDatabase.setText("company");  // Default database name
	txtDatabase.setBounds(90, 20, 193, 20);
	contentPane.add(txtDatabase);
	txtDatabase.setColumns(10);
		
		JButton btnDBFill = new JButton("Fill");
		/**
		 * The btnDBFill should fill the department and project JList with the 
		 * departments and projects from your entered database name.
		 */
	btnDBFill.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String dbName = txtDatabase.getText().trim();
			if (dbName.isEmpty()) {
				JOptionPane.showMessageDialog(EmployeeSearchFrame.this, 
					"Please enter a database name.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// Close existing connection if any
			closeConnection();
			
			// Connect to database and load data
			if (connectToDatabase(dbName)) {
				loadDepartments();
				loadProjects();
				JOptionPane.showMessageDialog(EmployeeSearchFrame.this, 
					"Successfully loaded departments and projects from database: " + dbName, 
					"Success", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	});
		
		btnDBFill.setFont(new Font("Times New Roman", Font.BOLD, 12));
		btnDBFill.setBounds(307, 19, 68, 23);
		contentPane.add(btnDBFill);
		
		JLabel lblDepartment = new JLabel("Department");
		lblDepartment.setFont(new Font("Times New Roman", Font.BOLD, 12));
		lblDepartment.setBounds(52, 63, 89, 14);
		contentPane.add(lblDepartment);
		
		JLabel lblProject = new JLabel("Project");
		lblProject.setFont(new Font("Times New Roman", Font.BOLD, 12));
		lblProject.setBounds(255, 63, 47, 14);
		contentPane.add(lblProject);
		
	lstProject = new JList<String>(new DefaultListModel<String>());
	lstProject.setFont(new Font("Tahoma", Font.PLAIN, 12));
	lstProject.setModel(project);
	lstProject.setBounds(225, 84, 150, 42);
	contentPane.add(lstProject);
	
	chckbxNotDept = new JCheckBox("Not");
	chckbxNotDept.setBounds(71, 133, 59, 23);
	contentPane.add(chckbxNotDept);
	
	chckbxNotProject = new JCheckBox("Not");
	chckbxNotProject.setBounds(270, 133, 59, 23);
	contentPane.add(chckbxNotProject);
		
		lstDepartment = new JList<String>(new DefaultListModel<String>());
		lstDepartment.setBounds(36, 84, 172, 40);
		contentPane.add(lstDepartment);
		lstDepartment.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lstDepartment.setModel(department);
		
		JLabel lblEmployee = new JLabel("Employee");
		lblEmployee.setFont(new Font("Times New Roman", Font.BOLD, 12));
		lblEmployee.setBounds(52, 179, 89, 14);
		contentPane.add(lblEmployee);
		
	JButton btnSearch = new JButton("Search");
	btnSearch.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (connection == null) {
				JOptionPane.showMessageDialog(EmployeeSearchFrame.this, 
					"Please connect to database first by clicking Fill button.", 
					"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			searchEmployees();
		}
	});
	btnSearch.setBounds(80, 276, 89, 23);
	contentPane.add(btnSearch);
		
	JButton btnClear = new JButton("Clear");
	btnClear.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			textAreaEmployee.setText("");
			lstDepartment.clearSelection();
			lstProject.clearSelection();
			chckbxNotDept.setSelected(false);
			chckbxNotProject.setSelected(false);
		}
	});
	btnClear.setBounds(236, 276, 89, 23);
	contentPane.add(btnClear);
		
	textAreaEmployee = new JTextArea();
	textAreaEmployee.setBounds(36, 197, 339, 68);
	contentPane.add(textAreaEmployee);
}

/**
 * Establishes a connection to the specified database
 * @param dbName The name of the database to connect to
 * @return true if connection successful, false otherwise
 */
private boolean connectToDatabase(String dbName) {
	try {
		// Load MySQL JDBC driver
		Class.forName("com.mysql.cj.jdbc.Driver");
		
		// Establish connection
		String url = DB_URL_PREFIX + dbName + "?useSSL=false&serverTimezone=UTC";
		connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
		return true;
		
	} catch (ClassNotFoundException e) {
		JOptionPane.showMessageDialog(this, 
			"MySQL JDBC Driver not found. Please add mysql-connector-java to classpath.", 
			"Driver Error", JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
		return false;
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(this, 
			"Failed to connect to database: " + e.getMessage(), 
			"Database Error", JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
		return false;
	}
}

/**
 * Loads all departments from the database into the department list
 */
private void loadDepartments() {
	department.clear();
	String query = "SELECT Dname, Dnumber FROM DEPARTMENT ORDER BY Dname";
	
	try (PreparedStatement stmt = connection.prepareStatement(query);
		 ResultSet rs = stmt.executeQuery()) {
		
		while (rs.next()) {
			String deptName = rs.getString("Dname");
			int deptNum = rs.getInt("Dnumber");
			department.addElement(deptName + " (" + deptNum + ")");
		}
		
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(this, 
			"Error loading departments: " + e.getMessage(), 
			"Database Error", JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
	}
}

/**
 * Loads all projects from the database into the project list
 */
private void loadProjects() {
	project.clear();
	String query = "SELECT Pname, Pnumber FROM PROJECT ORDER BY Pname";
	
	try (PreparedStatement stmt = connection.prepareStatement(query);
		 ResultSet rs = stmt.executeQuery()) {
		
		while (rs.next()) {
			String projName = rs.getString("Pname");
			int projNum = rs.getInt("Pnumber");
			project.addElement(projName + " (" + projNum + ")");
		}
		
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(this, 
			"Error loading projects: " + e.getMessage(), 
			"Database Error", JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
	}
}

/**
 * Searches for employees based on selected departments and projects
 * Handles multiple selections and NOT conditions
 */
private void searchEmployees() {
	textAreaEmployee.setText("");
	
	List<String> selectedDepts = lstDepartment.getSelectedValuesList();
	List<String> selectedProjs = lstProject.getSelectedValuesList();
	boolean notDept = chckbxNotDept.isSelected();
	boolean notProj = chckbxNotProject.isSelected();
	
	// Build the SQL query dynamically
	StringBuilder query = new StringBuilder();
	query.append("SELECT DISTINCT E.Fname, E.Minit, E.Lname, E.Ssn, E.Salary, ");
	query.append("D.Dname, E.Dno FROM EMPLOYEE E ");
	query.append("LEFT JOIN DEPARTMENT D ON E.Dno = D.Dnumber ");
	
	// Add WORKS_ON join if projects are selected
	if (!selectedProjs.isEmpty()) {
		query.append("LEFT JOIN WORKS_ON W ON E.Ssn = W.Essn ");
	}
	
	query.append("WHERE 1=1 ");
	
	// Add department conditions
	if (!selectedDepts.isEmpty()) {
		int[] deptNumbers = extractNumbers(selectedDepts);
		query.append("AND E.Dno ");
		if (notDept) {
			query.append("NOT ");
		}
		query.append("IN (");
		for (int i = 0; i < deptNumbers.length; i++) {
			query.append(deptNumbers[i]);
			if (i < deptNumbers.length - 1) query.append(", ");
		}
		query.append(") ");
	}
	
	// Add project conditions
	if (!selectedProjs.isEmpty()) {
		int[] projNumbers = extractNumbers(selectedProjs);
		query.append("AND ");
		if (notProj) {
			query.append("E.Ssn NOT IN (SELECT Essn FROM WORKS_ON WHERE Pno IN (");
		} else {
			query.append("W.Pno IN (");
		}
		for (int i = 0; i < projNumbers.length; i++) {
			query.append(projNumbers[i]);
			if (i < projNumbers.length - 1) query.append(", ");
		}
		if (notProj) {
			query.append(")) ");
		} else {
			query.append(") ");
		}
	}
	
	query.append("ORDER BY E.Lname, E.Fname");
	System.out.println("Query is "+ query);
}

/**
 * Extracts department or project numbers from items to "Name (Number)"
 * @param selectedItems List of selected items
 * @return Array of extracted numbers
 */
private int[] extractNumbers(List<String> selectedItems) {
	int[] numbers = new int[selectedItems.size()];
	for (int i = 0; i < selectedItems.size(); i++) {
		String item = selectedItems.get(i);
		int startIdx = item.lastIndexOf("(") + 1;
		int endIdx = item.lastIndexOf(")");
		numbers[i] = Integer.parseInt(item.substring(startIdx, endIdx));
	}
	return numbers;
}

/**
 * Closes the database connection
 */
private void closeConnection() {
	if (connection != null) {
		try {
			connection.close();
			connection = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
}
