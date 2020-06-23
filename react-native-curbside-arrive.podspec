require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-curbside-arrive"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = "The Curbside ARRIVE SDK lets you build apps that produce reliable alerts when customers are approaching your sites. ARRIVE provides predictive alerts based on time to arrival that are more reliable than simpler solutions such as geofencing. Notifications can be sent to any in-store system, point-of-sale or in-store device via webhooks or by using the SDK for on-premise native applications. ARRIVE dashboard is a dashboard showing your sites, the average wait times out front, and other mission critical customer activity.\nhttps://developer.rakutenready.com"
  s.homepage     = "https://developer.rakutenready.com"
  s.license      = package['license']
  s.authors      = "Rakuten Ready"
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/RakutenReady/react-native-curbside-arrive.git", :tag => s.version.to_s}
  s.source_files = "ios/**/*.{h,c,m,swift}"
  s.requires_arc = true
  s.default_subspec = 'Default'

  s.dependency "React"

  s.subspec 'Default' do |ss|
    ss.dependency 'Curbside', '<= 3.31'
  end
  
end

