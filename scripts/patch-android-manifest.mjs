import { readFile, writeFile } from 'node:fs/promises';

const path = 'android/app/src/main/AndroidManifest.xml';
let xml = await readFile(path, 'utf8');
const permissions = [
  '    <uses-permission android:name="android.permission.CAMERA" />',
  '    <uses-feature android:name="android.hardware.camera" android:required="false" />',
];
for (const line of permissions) {
  const name = line.match(/android:name="([^"]+)"/)?.[1];
  if (name && !xml.includes(name)) xml = xml.replace('<application', line + '\n    <application');
}
await writeFile(path, xml);
console.log('Android camera permission configured.');
