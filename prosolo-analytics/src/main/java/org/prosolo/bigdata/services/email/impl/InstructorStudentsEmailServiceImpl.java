package org.prosolo.bigdata.services.email.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.CourseDAO;
import org.prosolo.bigdata.dal.persistence.UserDAO;
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl;
import org.prosolo.bigdata.dal.persistence.impl.UserDAOImpl;
import org.prosolo.bigdata.email.EmailSender;
import org.prosolo.bigdata.scala.spark.emails.CourseInstructorEmail;
import org.prosolo.bigdata.services.email.InstructorStudentsEmailService;
import org.prosolo.bigdata.services.email.instructorEmail.emailGenerator.InstructorEmailGenerator;
import org.prosolo.common.config.AppConfig;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.email.generators.EmailContentGenerator;
import org.prosolo.common.util.Pair;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class InstructorStudentsEmailServiceImpl implements InstructorStudentsEmailService {
	
	private static Logger logger = Logger.getLogger(InstructorStudentsEmailServiceImpl.class);
	
	CourseDAO courseDAO = new CourseDAOImpl(true);
	UserDAO userDAO =  UserDAOImpl.getInstance();

	@Override
	public void sendEmailToInstructor(long courseId, long instructorId, List<Long> assignedStudents, 
			List<Long> unassignedStudents) {
		System.out.println("SEND EMAIL TO INSTRUCTOR:"+instructorId);
		EmailSender emailSender = new EmailSender();
		Pair<String,InstructorEmailGenerator> emailGeneratorPair=createEmailGenerator(courseId,instructorId,assignedStudents,unassignedStudents);
		if(emailGeneratorPair!=null){
			try {
				emailSender.sendEmail(emailGeneratorPair.getSecond(),emailGeneratorPair.getFirst());
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
	private Pair<String,InstructorEmailGenerator> createEmailGenerator(long courseId, long instructorId, List<Long> assignedStudents,
												   List<Long> unassignedStudents){

		Map<String, String> res = userDAO.getUserNameAndEmail(instructorId);
		String instructorName = res.get("name");
		String email = res.get("email");

		if (email != null) {
			String courseName = courseDAO.getCredentialTitle(courseId);
			List<String> assigned = userDAO.getUserNames(assignedStudents);
			List<String> unassigned = userDAO.getUserNames(unassignedStudents);

			InstructorEmailGenerator generator = new InstructorEmailGenerator(instructorName,
					courseName, assigned, unassigned);



			if (CommonSettings.getInstance().config.appConfig.projectMode.equals(AppConfig.ProjectMode.DEV)) {
				email = CommonSettings.getInstance().config.appConfig.developerEmail;
			}
			return new Pair<String,InstructorEmailGenerator>(email,generator);
		}
		return null;
	}

	@Override
	public void sendEmailsToInstructors(List<CourseInstructorEmail> batchEmails) {
		Map<EmailContentGenerator,String> instructorEmailGeneratorMap= batchEmails.stream().map(
			batchEmail-> {
				Pair<String,InstructorEmailGenerator> emailGeneratorPair=createEmailGenerator(batchEmail.courseId(),batchEmail.instructorId(),batchEmail.assigned(),batchEmail.unassigned());

				return emailGeneratorPair;
			}
			).collect(Collectors.toMap( p->  p.getSecond(),p->p.getFirst()));

		EmailSender emailSender = new EmailSender();
		try{
			emailSender.sendBatchEmails(instructorEmailGeneratorMap);
		}catch(AddressException ex){
			logger.error(ex);
		}


	}



}
