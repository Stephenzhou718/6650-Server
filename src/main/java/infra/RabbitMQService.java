package infra;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class RabbitMQService {

  private Connection connection;
  private Channel channel;
  private final String host;

  public RabbitMQService(String host) throws Exception {
    this.host = host;
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(this.host);
    factory.setUsername("admin");
    factory.setPassword("admin");
    factory.setVirtualHost("/");
    // Set other connection properties here, such as username and password if necessary
    this.connection = factory.newConnection();
  }

  public Channel getOrCreateChannel(String queueName) throws Exception {
    if (this.channel != null && this.channel.isOpen()) {
      return this.channel;
    } else {
      this.channel = connection.createChannel();
      this.channel.queueDeclare(queueName, false, false, false, null);
      return this.channel;
    }
  }

  public void sendMessage(String queueName, String message) throws Exception {
    Channel channel = getOrCreateChannel(queueName);
    channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + message + "'");
  }

  public void closeChannel() throws Exception {
    if (this.channel != null && this.channel.isOpen()) {
      this.channel.close();
    }
  }

  public void closeConnection() throws Exception {
    if (this.connection != null && this.connection.isOpen()) {
      this.connection.close();
    }
  }

  // Ensure resources are released when the object is garbage-collected
  @Override
  protected void finalize() throws Throwable {
    try {
      closeChannel();
      closeConnection();
    } finally {
      super.finalize();
    }
  }

  // The rest of your RabbitMQ logic would go here
}
