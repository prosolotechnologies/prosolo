package org.prosolo.bigdata.services.email.instructorEmail.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.CourseDAO;
import org.prosolo.bigdata.dal.persistence.UserDAO;
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl;
import org.prosolo.bigdata.dal.persistence.impl.UserDAOImpl;
import org.prosolo.bigdata.email.EmailSender;
import org.prosolo.bigdata.services.email.instructorEmail.InstructorStudentsEmailService;
import org.prosolo.bigdata.services.email.instructorEmail.emailGenerator.InstructorEmailGenerator;
import org.prosolo.common.config.CommonSettings;

public class InstructorStudentsEmailServiceImpl implements InstructorStudentsEmailService {
	
	private static Logger logger = Logger.getLogger(InstructorStudentsEmailServiceImpl.class);
	
	CourseDAO courseDAO = new CourseDAOImpl();
	UserDAO userDAO =  UserDAOImpl.getInstance();

	@Override
	public void sendEmailToInstructor(long courseId, long instructorId, List<Long> assignedStudents, 
			List<Long> unassignedStudents) {
		Map<String, String> res = userDAO.getUserNameAndEmail(instructorId); 
		String instructorName = res.get("name");
		String email = res.get("email");
		
		if (email != null) {
			String courseName = courseDAO.getCredentialTitle(courseId);
			List<String> assigned = userDAO.getUserNames(assignedStudents);
			List<String> unassigned = userDAO.getUserNames(unassignedStudents);
			
			InstructorEmailGenerator generator = new InstructorEmailGenerator(instructorName,
					courseName, assigned, unassigned);

			EmailSender emailSender = new EmailSender();
			
			if (CommonSettings.getInstance().config.appConfig.developmentMode) {
				email = CommonSettings.getInstance().config.appConfig.developmentEmail;
			}
			
			try {
				emailSender.sendEmail(generator, email);
			} catch (AddressException e) {
				logger.error(e);
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				logger.error(e);
				e.printStackTrace();
			} catch (MessagingException e) {
				logger.error(e);
				e.printStackTrace();
			} catch (IOException e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
	}
}
