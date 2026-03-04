const { execSync } = require('child_process');

const args = process.argv.slice(2);

if (args.includes('java')) {
    console.log('Running Java conversion...');
    try {
        execSync('npm run build-java', { stdio: 'inherit' });
    } catch (error) {
        console.error('Java conversion failed');
        process.exit(1);
    }
} else {
    console.log('Skipping Java conversion (use "npm run build -- java" to include it)');
}
