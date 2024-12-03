# Autonomous Content Creator (AutoCC)

AutoCC is a Java application that automatically generates content using OpenAI's GPT models (or basically any other model if you decide to tweak it) and posts it to social media platforms (Facebook and Instagram). While the default implementation focuses on biology facts, the system can be easily adapted for any topic.

## Features

- Automatic content generation using OpenAI's GPT models
- Automated posting to Facebook Pages
- Image generation with text overlay
- Instagram posting capability
- Topic rotation system for varied content
- Configurable themes and topics

## Prerequisites

- Java 11 or higher
- Maven
- Facebook Developer Account
- Instagram Business Account
- OpenAI API Account

## Setup Instructions

### 1. Configure API Credentials

Create a file named `application.properties` in `src/main/resources/` with the following structure:

```properties
# OpenAI Configuration
openai.api.key=your_openai_api_key

# Facebook Configuration
facebook.page.token=your_facebook_page_access_token
facebook.page.id=your_facebook_page_id

# Instagram Configuration (Optional)
instagram.access.token=your_instagram_access_token
instagram.business.id=your_instagram_business_id
```

### 2. How to Obtain Credentials

#### OpenAI API Key
1. Go to [OpenAI's platform](https://platform.openai.com/)
2. Create an account or log in
3. Navigate to API Keys section
4. Generate a new API key

#### Facebook Page Token
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app or select existing one
3. Add the Facebook Pages API
4. Generate a page access token for your page
5. Get your page ID from your Facebook page's About section

#### Instagram Business Account
1. Convert your Instagram account to a Business account
2. Link it to your Facebook page
3. Use the Facebook Graph API to get the Instagram business account ID
4. Generate an access token with instagram_basic and instagram_content_publish permissions

### 3. Building the Project

```bash
mvn clean install
```

### 4. Running the Application

```bash
java -jar target/autonomous-content-creator-1.0.jar
```

## Customizing Content Theme

To change the theme of the posts (e.g., from biology to another topic):

1. Modify the prompt in `AutoCC.java`:
```java
// In the generateBiologyFact() method, change the prompt:
ChatMessage userMessage = new ChatMessage("user", "Provide an interesting fact about [YOUR_TOPIC].");
```

2. Update the TopicRotation class:
- Open `TopicRotation.java`
- Replace the topics in the `initializeTopics()` method with your desired topics
- Update the method name (e.g., from `generateBiologyFact()` to `generateFact()`)

Example Topic Categories:
- History facts
- Space exploration
- Technology innovations
- Psychology insights
- Art history
- Movie trivia
- Scientific discoveries
- Mathematical concepts
- Cultural traditions
- Cooking tips

## File Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── autocc/
│   │           └── autonomouscontentcreator/
│   │               ├── AutoCC.java
│   │               ├── Main.java
│   │               ├── PropertiesConfig.java
│   │               └── TopicRotation.java
│   └── resources/
│       └── application.properties
```
