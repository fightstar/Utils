

import com.tibco.tibjms.TibjmsQueueConnectionFactory;
import com.tibco.tibjms.TibjmsTopicConnectionFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


public class UtilsJMS  implements Runnable {

	static HashMap<String, String> props = new HashMap<String, String>();
	static int numberMessagesToRead = 0;
	static Report rep;
	static List<String> messages = new ArrayList<String>();
	Properties properties = new Properties ();

	public UtilsJMS (Properties properties, int numberMessagesToRead,Report rep)
	{
		this.properties = properties;
		this.numberMessagesToRead = numberMessagesToRead;
		this.rep = rep;
	}

	public static String queuePublisher(HashMap<String, String> props, String xmlmessage) {
		String JMSMessageID = null;
		QueueConnection connection = null;
		Common.print(props.get("JMS_URL") + ":" + props.get("JMS_PORT") + ":::" + props.get("JMS_USER") + ":::" + props.get("JMS_PASS"));
		TibjmsQueueConnectionFactory factory = new com.tibco.tibjms.TibjmsQueueConnectionFactory(props.get("JMS_URL") + ":" + props.get("JMS_PORT"));
		//			Common.print("TibjmsQueueConnectionFactory");
		try {
			connection = factory.createQueueConnection(props.get("JMS_USER"), props.get("JMS_PASS"));
			//			Common.print("QueueConnection");
			QueueSession session = connection.createQueueSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			//			Common.print("QueueSession");
			javax.jms.Queue Queue;
			String mess = Common.readFileToString(xmlmessage);
			if (mess.contains("ta_control_message"))
				Queue = session.createQueue(props.get("JMS_QUEUE_CONTROL"));
			else
				Queue = session.createQueue(props.get("JMS_QUEUE_TADS"));
			//			Common.print("createQueue");

			QueueSender sender = session.createSender(Queue);
			/* publish messages */
			javax.jms.TextMessage message = session.createTextMessage();
			//				Common.print("createTextMessage");
			message.setText(mess);
			sender.send(message);
			//????				session.commit(); //for transacted sessions
			Common.print("Sent message: " + xmlmessage);
			Common.print("Result:true - Message ID:" + message.getJMSMessageID());
			JMSMessageID = xmlmessage + " ::: " + message.getJMSMessageID();
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
			JMSMessageID = "Result:false - message sending fails";
		}
		return JMSMessageID;
	}

	public static String topicPublisher(HashMap<String, String> props, String xmlmessage) {
		String JMSMessageID = null;
		TopicConnection connection = null;
		Common.print(props.get("JMS_URL") + ":" + props.get("JMS_PORT") + ":::" + props.get("JMS_USER") + ":::" + props.get("JMS_PASS"));
		TibjmsTopicConnectionFactory factory = new com.tibco.tibjms.TibjmsTopicConnectionFactory(props.get("JMS_URL") + ":" + props.get("JMS_PORT"));
		//			Common.print("TibjmsQueueConnectionFactory");
		try {
			connection = factory.createTopicConnection(props.get("JMS_USER"), props.get("JMS_PASS"));
			//			Common.print("QueueConnection");
			TopicSession session = connection.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			//			Common.print("QueueSession");
			String mess = Common.readFileToString(xmlmessage);
			Topic Topic = session.createTopic(props.get("TIBCO_JMS_TOPIC"));
			//			Common.print("createQueue");

			TopicPublisher publisher = session.createPublisher(Topic);
			/* publish messages */
			javax.jms.TextMessage message = session.createTextMessage();
			//				Common.print("createTextMessage");
			message.setText(mess);
			publisher.publish(message);//.send(message);
			//????				session.commit(); //for transacted sessions
			Common.print("Sent message: " + xmlmessage);
			Common.print("Message ID:" + message.getJMSMessageID());
			JMSMessageID = xmlmessage + " ::: " + message.getJMSMessageID();
			connection.close();
		} catch (JMSException e) {
			JMSMessageID = "Result:false - message publishing fails";
			e.printStackTrace();
		}
		return JMSMessageID;
	}

	public static List<String> receiveMessageFromQueueTest(HashMap<String, String> props, Integer numberMessagesToRead) throws Exception {

		String queueName = props.get("QUEUE_TO_READ");
		List<String> messages = new ArrayList<String>();
		Common.print(props.get("JMS_URL") + ":" + props.get("JMS_PORT") + ":::" + props.get("JMS_USER") + ":::" + props.get("JMS_PASS"));
		Common.print("Receiving from queue: " + queueName + "\n");

		try {
			QueueConnectionFactory factory = new com.tibco.tibjms.TibjmsQueueConnectionFactory(props.get("JMS_URL") + ":" + props.get("JMS_PORT"));
			QueueConnection connection = factory.createQueueConnection(props.get("JMS_USER"), props.get("JMS_PASS"));

			QueueSession session = connection.createQueueSession(false, javax.jms.Session.CLIENT_ACKNOWLEDGE);

			javax.jms.Queue queue = session.createQueue(queueName);

			QueueReceiver receiver = session.createReceiver(queue);

			connection.start();
			Integer i = 0;

			long duration = Long.parseLong(props.get("TIMEOUT"));
			long startTime = System.currentTimeMillis(); //fetch starting time

			/*while( i < numberMessagesToRead)
    		{
           	 Common.print("Reading message: " + i + " from " + numberMessagesToRead);
//                javax.jms.TextMessage message = (TextMessage) receiver.receive();
                javax.jms.Message message = receiver.receive(duration);
                Common.print("Received message: "+ message);
                 if (message == null) 
                {
                	Common.print("No more messages to read");
                	break;
                }
                 else
                     messages.add(message.toString());
                i++;
            }*/
			int n = 1;
			for (int in = 0; in < 5; in++) {
				javax.jms.Message message = receiver.receive(duration);
				if (message == null) {
					Common.print("No more messages to read");
					break;
				}
				messages.add(message.toString());
				Common.print("message: " + n);
				Common.print("Received message: " + message);
				n++;
			}

			Common.print("No more messages to read");
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return messages;
	}

	public static String getTextFromQueueMessage(Properties props, String queueName) throws JMSException {

		String messageText = "";
		try {
			QueueConnectionFactory factory = new com.tibco.tibjms.TibjmsQueueConnectionFactory(props.getProperty("JMS_URL") + ":" + props.getProperty("JMS_PORT"));

			QueueConnection connection = factory.createQueueConnection(props.getProperty("JMS_USER"), props.getProperty("JMS_PASS"));

			QueueSession session = connection.createQueueSession(false, javax.jms.Session.CLIENT_ACKNOWLEDGE);

			javax.jms.Queue queue = session.createQueue(queueName);

			QueueReceiver receiver = session.createReceiver(queue);

			connection.start();

			long duration = Long.parseLong(props.getProperty("TIMEOUT"));

			javax.jms.Message message = receiver.receive(duration);
			messageText = message.toString();

			connection.close();

		} catch (JMSException | NullPointerException e) {
			e.printStackTrace();
		}
		return messageText;
	}

	public static String getTextFromTopicMessage(Properties props, String queueName) throws JMSException {

		Common.print("start listening");
		String messageText = "";
		try {
			TopicConnectionFactory factory = new com.tibco.tibjms.TibjmsTopicConnectionFactory(props.getProperty("JMS_URL") + ":" + props.getProperty("JMS_PORT"));

			TopicConnection connection = factory.createTopicConnection(props.getProperty("JMS_USER"), props.getProperty("JMS_PASS"));

			TopicSession session = connection.createTopicSession(false, javax.jms.Session.CLIENT_ACKNOWLEDGE);
			Topic topicName = session.createTopic(queueName);
			
			javax.jms.TopicSubscriber topic = session.createSubscriber(topicName);
			connection.start();
			Common.print("start session");

			long duration = Long.parseLong(props.getProperty("TIMEOUT"));
/*
//new
			for (int in = 0; in < 2; in++) {
				javax.jms.TextMessage message = (TextMessage) topic.receive(duration);
//				javax.jms.TextMessage message = (TextMessage) topic.receive();
				if (message == null) {
					Common.print("No more messages to read");
					break;
				}
				messages.add(message.getText());
				Common.print("Received message: " + message.getText());
			}
//end new	
*/			
			javax.jms.TextMessage message = (TextMessage) topic.receive(duration);
			Common.print("message recieved" + message.getText());
			messageText = message.getText();
			connection.close();

		} catch (JMSException | NullPointerException e) {
			e.printStackTrace();
			Common.print("jms catch");
		}	
		return messageText;
	}

	public static void clearMessagesFromQueue(HashMap<String, String> props, String queueName) throws JMSException {
		try {
			QueueConnectionFactory factory = new com.tibco.tibjms.TibjmsQueueConnectionFactory(props.get("JMS_URL") + ":" + props.get("JMS_PORT"));

			QueueConnection connection = factory.createQueueConnection(props.get("JMS_USER"), props.get("JMS_PASS"));

			QueueSession session = connection.createQueueSession(false, javax.jms.Session.CLIENT_ACKNOWLEDGE);

			javax.jms.Queue queue = session.createQueue(queueName);

			QueueReceiver receiver = session.createReceiver(queue);

			connection.start();

			Message message = null;
			do {
				message = receiver.receiveNoWait();
				if (message != null) message.acknowledge();
			}
			while (message != null);
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static List<String> returnRecievedMessages ()
	{
		return messages;
	}

	@Override
	public void run() {
		 while (!Thread.currentThread().isInterrupted()) {
			 String queueName = properties.getProperty("QUEUE_TO_READ");
		Common.print(properties.getProperty("JMS_URL") + ":" + properties.getProperty("JMS_PORT") + ":::" + properties.getProperty("JMS_USER") + ":::" + properties.getProperty("JMS_PASS"));
		Common.print("Receiving from queue: " + queueName + "\n");

		try {
			QueueConnectionFactory factory = new com.tibco.tibjms.TibjmsQueueConnectionFactory(properties.getProperty("JMS_URL") + ":" + properties.getProperty("JMS_PORT"));
			QueueConnection connection = factory.createQueueConnection(properties.getProperty("JMS_USER"), properties.getProperty("JMS_PASS"));
			QueueSession session = connection.createQueueSession(false, javax.jms.Session.CLIENT_ACKNOWLEDGE);
			javax.jms.Queue queue = session.createQueue(queueName);
			QueueReceiver receiver = session.createReceiver(queue);
			connection.start();
			Integer i = 0;

			long duration = Long.parseLong(properties.getProperty("JMS_TIMEOUT"));
			long startTime = System.currentTimeMillis(); //fetch starting time

			while (i < numberMessagesToRead) {
				Common.print("Reading message: " + i + " from " + numberMessagesToRead);
				javax.jms.Message message = receiver.receive(duration);
				Common.print("Received message: " + message);
				if (message == null) {
					Common.print("No more messages to read");
					break;
				} else
					messages.add(message.toString());
				i++;
			}
			connection.close();
			String status = i == 0 ? "FAIL" : "PASS";
			TemplateActions.updateReport(rep, "Message reciving", status, messages.toString());
		} catch (JMSException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	}
}

class Chat implements javax.jms.MessageListener
{
public void onMessage(Message message){
try{
	javax.jms.TextMessage textMessage = (TextMessage)message;
    String text = textMessage.getText( );
    Common.print(text);
} catch (JMSException jmse){jmse.printStackTrace( );}
}

}


/*
public Message readMessageFromRetryQueueByJmsId(String jmsId) throws QueueingException {
Connection connection = null;
Session session = null;
MessageConsumer messageConsumer = null;
Message message = null;
try {
    connection = getConnectionFactory().createConnection();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    connection.start();
    messageConsumer = session.createConsumer(getRetryQueue(), "JMSMessageID='"+jmsId+"'");
    message = messageConsumer.receiveNoWait();
} catch (JMSException e) {
    throw new QueueingException("Failed to read message from MessageConsumer.");
} finally {
    try { connection.close(); } catch (Exception e) {}
}
return message;
}
 */




/*
public class Threads {

public static void main(String[] args) {
Runnable r = new Runnable1();
Thread t = new Thread(r);
Runnable r2 = new Runnable2();
Thread t2 = new Thread(r2);
t.start();
t2.start();
}
}

class Runnable2 implements Runnable{
public void run(){
for(int i=0;i<11;i+=2) {
    System.out.println(i);
}
}
}

class Runnable1 implements Runnable{
public void run(){
for(int i=1;i<=11;i+=2) {
   System.out.println(i);
}
}
}
 */
