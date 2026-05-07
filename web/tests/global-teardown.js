async function globalTeardown(config) {
  console.log('🧹 Starting global test teardown...');
  
  // Clean up test environment
  const fs = await import('fs/promises');
  const path = await import('path');
  
  // Generate test summary
  const testResultsDir = path.join(process.cwd(), 'test-results');
  const summaryPath = path.join(testResultsDir, 'test-summary.json');
  
  try {
    const summary = {
      timestamp: new Date().toISOString(),
      testEnvironment: 'playwright',
      browsers: ['chromium', 'firefox', 'webkit'],
      mobile: ['Mobile Chrome', 'Mobile Safari'],
      screenshots: 'test-results/screenshots',
      videos: 'test-results/videos',
      traces: 'test-results/traces'
    };
    
    await fs.writeFile(summaryPath, JSON.stringify(summary, null, 2));
    console.log('✅ Test summary generated');
  } catch (error) {
    console.error('❌ Error generating test summary:', error);
  }
  
  console.log('✅ Global teardown completed');
}

export default globalTeardown;
