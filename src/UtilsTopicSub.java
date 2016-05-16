

import javax.jms.*;
import java.util.ArrayList;
import java.util.Properties;

public class UtilsTopicSub implements MessageListener {

  TopicConnection topicConnection = null;   
  boolean subscriptionOn;
  static private ArrayList<String> messages = new ArrayList<String>();
  static int expectedMessagesCount = 0;
  static long duration = 0;
  static long startTime = 0;
   
  public static ArrayList<String> get(Properties props, String topicName, int messagesNumber) {
	  UtilsTopicSub dbpTopic = new UtilsTopicSub(props, topicName); 
		expectedMessagesCount = messagesNumber;
		startTime = System.currentTimeMillis();
		Boolean stillWaiting = true;
      try {
      while (dbpTopic.subscriptionOn && stillWaiting) 
      {
    	  Thread.sleep(10000);
          Common.print("messages.size(): " + messages.size());

          if ( System.currentTimeMillis() - startTime > duration) {
        	  stillWaiting = false;
            }
      } 
        //onMessage() waits for message here
      } catch (java.lang.InterruptedException intExc) {} 
      try {
    	  dbpTopic.topicConnection.close();
      } catch (javax.jms.JMSException jmsEx) {
          System.out.println("JMS Exception: " + jmsEx.toString());
      }
      System.out.println("JMS session is closed");
      return messages;
  }

  
  private UtilsTopicSub(Properties props, String topicName) {
		Common.print("start listening");
		try {
			TopicConnectionFactory factory = new com.tibco.tibjms.TibjmsTopicConnectionFactory(props.getProperty("JMS_URL") + ":" + props.getProperty("JMS_PORT"));
			this.subscriptionOn = true;
			duration = Long.parseLong(props.getProperty("TIMEOUT"));	 	  

			topicConnection = factory.createTopicConnection(props.getProperty("JMS_USER"), props.getProperty("JMS_PASS"));

			TopicSession session = topicConnection.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			Topic topic = session.createTopic(topicName);
			
			TopicSubscriber topicSubscriber = session.createSubscriber(topic);
	        topicSubscriber.setMessageListener(this);
			topicConnection.start();
			Common.print("start session");  
      } 
		catch (javax.jms.JMSException jmsEx) {
        System.out.println("JMS Exception: " + 
                            jmsEx.toString());
      }
  }

  public void onMessage(Message message)
  {
		Common.print("onMessage start");  
      try {
          TextMessage textMessage = (TextMessage)message;
          Common.print(" receiving line " + " : " + 
                               textMessage.getText());
          Common.print(messages.size() + " ::: " + this.subscriptionOn);
          messages.add(textMessage.getText());
          if (messages.size() >= expectedMessagesCount) {
              this.subscriptionOn = false;
            }          
          Common.print(messages.size() + " ::: " + this.subscriptionOn);
      } catch (javax.jms.JMSException jmsEx) {
         System.out.println("JMS Exception in onMessage: " + 
                             jmsEx.toString());
         } 
		Common.print("onMessage end");  
  }
}

