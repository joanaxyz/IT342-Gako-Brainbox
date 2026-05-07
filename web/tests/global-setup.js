async function globalSetup(config) {
  console.log('🚀 Starting global test setup...');
  
  // Set up test environment variables
  process.env.TEST_USERNAME = 'joana';
  process.env.TEST_PASSWORD = 'joana123456';
  
  // Create test results directory if it doesn't exist
  const fs = await import('fs/promises');
  const path = await import('path');
  
  const testResultsDir = path.join(process.cwd(), 'test-results');
  try {
    await fs.mkdir(testResultsDir, { recursive: true });
    console.log('✅ Test results directory created');
  } catch (error) {
    console.log('ℹ️ Test results directory already exists');
  }
  
  // Create screenshots directory
  const screenshotsDir = path.join(testResultsDir, 'screenshots');
  try {
    await fs.mkdir(screenshotsDir, { recursive: true });
    console.log('✅ Screenshots directory created');
  } catch (error) {
    console.log('ℹ️ Screenshots directory already exists');
  }
  
  console.log('✅ Global setup completed');
}

export default globalSetup;
