/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */
 
'use strict';
import React, {Component} from 'react';
import {
  Alert,
  FlatList,
  Linking,
  Platform,
  PermissionsAndroid,
  SafeAreaView,
  StyleSheet,
  ScrollView,
  View,
  Text,
  TextInput,
  ToastAndroid,
  TouchableHighlight,
  StatusBar,
} from 'react-native';

import {
  Header,
  LearnMoreLinks,
  Colors,
  DebugInstructions,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

import Button from './src/components/Button';
import FetchLocation from './src/components/FetchLocation';
import Geolocation from 'react-native-geolocation-service';
import messaging from '@react-native-firebase/messaging';

import { NativeModules } from 'react-native';

const curbside = NativeModules.Curbside;
// Piecompany
const USAGE_TOKEN = '5456fd004166c9226ec3f2cb1ec567f0556af771305cc3e38fb0c3419833f48b';

export default class App extends Component<{}> {

  constructor(props) {
    super(props)
    curbside.createSessionWithUsageToken(USAGE_TOKEN)
    this.state = { 
      user_location: null, 
      data_source: [],
      full_name: 'John Smith',
      email_address: 'john.smith@example.com',
      sms_number: '8883308304',
      car_make: 'Tesla',
      car_model: 'Model S',
      car_license_plate: 'NAP789'
    }
  }

  async componentDidMount() { 
    this.checkNotificationPermission();  
    this.hasLocationPermission();
  }

  hasLocationPermissionIOS = async () => {
    const openSetting = () => {
      Linking.openSettings().catch(() => {
        Alert.alert('Unable to open settings');
      });
    };
    const status = await Geolocation.requestAuthorization('whenInUse');

    if (status === 'granted') {
      return true;
    }

    if (status === 'denied') {
      Alert.alert('Location permission denied');
    }

    if (status === 'disabled') {
      Alert.alert(
        `Turn on Location Services to allow App to determine your location.`,
        '',
        [
          { text: 'Go to Settings', onPress: openSetting },
          { text: "Don't Use Location", onPress: () => {} },
        ],
      );
    }

    return false;
  };

  hasLocationPermission = async () => {
    if (Platform.OS === 'ios') {
      const hasPermission = await this.hasLocationPermissionIOS();
      return hasPermission;
    }

    if (Platform.OS === 'android' && Platform.Version < 23) {
      return true;
    }

    const hasPermission = await PermissionsAndroid.check(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
    );

    if (hasPermission) {
      return true;
    }

    const status = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
    );

    if (status === PermissionsAndroid.RESULTS.GRANTED) {
      return true;
    }

    if (status === PermissionsAndroid.RESULTS.DENIED) {
      ToastAndroid.show(
        'Location permission denied by user.',
        ToastAndroid.LONG,
      );
    } else if (status === PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN) {
      ToastAndroid.show(
        'Location permission revoked by user.',
        ToastAndroid.LONG,
      );
    }

    return false;
  };

  //Check whether Push Notifications are enabled or not
  async checkNotificationPermission() {
    const enabled = await messaging().hasPermission()
    if (enabled) {
      this.getToken()
    } else {
      this.requestNotificationPermission()
    }
  }

  //Get Device Registration Token
  async getToken() {
    const fcmToken = await messaging().getToken()
    if (fcmToken) {
      console.log('fcmToken:', fcmToken)
    }
  }

  //Request for Push Notification
  async requestNotificationPermission() {
    try {
      await messaging().requestPermission()
      // If user allow Push Notification
      this.getToken()
    } catch (error) {
      // If user do not allow Push Notification
      console.log('Rejected')
    }
  }

  getSiteLocationHandler = () => {
    Geolocation.getCurrentPosition(
      position => {
        this.setState({
          user_location: {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            latitudeDelta: 0.0622,
            longitudeDelta: 0.0421
          }
        });
        fetch("https://api.curbside.com/plt/ad/2017-05-31/sites?lat="+position.coords.latitude+"&lng="+position.coords.longitude, {
          method: "GET",
          headers: {
            'Curbside-Usage-Token':USAGE_TOKEN
          },
        })
          .then(Response => Response.json())
          .then(responseJson => {
              this.setState({
              //Fetching 5 nearby sites for now
              data_source: responseJson.ad_sites.slice(0, 5).map(
                // adding random key
                (item, key) => Object.assign(item, { key: `item_${key}_${Math.random().toString(36).substring(2, 7)}` })
              )
            })
          })
          .catch(err => console.log(err));
      },
      err => console.log(err),
    )
  }

  startTrack = () => {
    if(this.state.tracking_identifier && this.state.site_id) {
      this.setState({
        track_token: Math.random().toString(36).substring(2, 15),
        tracking_identifier: this.state.tracking_identifier,
        disabled: !Boolean(this.state.disabled)
      })
    } else {
      alert("Tracking Identifier, \n" + "Site ID are required to start a trip.")
    }
  }

  stopTrack = () => {
    if(this.state.tracking_identifier && this.state.site_id && this.state.track_token) {
        this.setState({
          disabled: false
        })
        curbside.completeTripToSiteWithIdentifier(this.state.site_id,this.state.track_token)
    } else {
      alert("You need to Start Trip before calling Stop Trip")
    }
  }

  cancelTrack = () => {
    if(this.state.tracking_identifier && this.state.site_id && this.state.track_token) {
        this.setState({
          disabled: false
        })
        curbside.cancelTripToSiteWithIdentifier(this.state.site_id,this.state.track_token)
    } else {
      alert("You need to Start Trip before calling Cancel Trip")
    }
  }

  render() {

    if(this.state.track_token && this.state.disabled === true && this.state.tracking_identifier) {
      curbside.setTrackingIdentifier(this.state.tracking_identifier)
      curbside.setUserInfo(this.state.full_name,this.state.email_address,this.state.sms_number,this.state.car_make,this.state.car_model,this.state.car_license_plate)
      curbside.startTripToSiteWithIdentifierWithTripType(this.state.site_id,this.state.track_token,'curbside')
    }
    return (
      <SafeAreaView style={styles.container}>
        <FetchLocation onGetLocation={this.getSiteLocationHandler} />
        {
          this.state.data_source.length > 0 &&
            <View style={styles.listWrapper}>
              <FlatList
                data={ this.state.data_source }
                ItemSeparatorComponent={() => <View style={styles.listSeparator} />}
                renderItem={ ({ item, separators }) => {
                  return (
                    <TouchableHighlight
                      underlayColor={styles.listHighlighted.backgroundColor}
                      onPress={() => {
                        this.setState({ site_id: item.site_id });
                      }}
                      onShowUnderlay={separators.highlight}
                      onHideUnderlay={separators.unhighlight}
                    >
                      <View style={styles.list} >
                        <Text>{ item.site_name }</Text>
                      </View>
                    </TouchableHighlight>
                  )
                } 
              }
            />
          </View>
          
        }
        <TextInput style={styles.Input} onChangeText={site_id => {
              this.setState({ site_id });
            }}
            placeholder="Site ID"
            value={this.state.site_id}
            autoCapitalize="none"
        />
        <TextInput style={styles.Input} onChangeText={tracking_identifier => {
              this.setState({ tracking_identifier });
            }}
            placeholder="TrackingIdentifier"
            value={this.state.tracking_identifier}
            autoCapitalize="none"
        />
        <TextInput style={styles.Input} onChangeText={full_name => {
              this.setState({ full_name });
            }}
            placeholder="Full Name"
            value={this.state.full_name}
            autoCapitalize="none"
        />
        <TextInput style={styles.Input} onChangeText={email_address => {
              this.setState({ email_address });
            }}
            placeholder="Email Address"
            value={this.state.email_address}
            autoCapitalize="none"
        />
        <TextInput style={styles.Input} onChangeText={sms_number => {
              this.setState({ sms_number });
            }}
            placeholder="Mobile Number"
            value={this.state.sms_number}
            autoCapitalize="none"
        />
        <TextInput style={styles.Input} onChangeText={car_make => {
              this.setState({ car_make });
            }}
            placeholder="Car Make"
            value={this.state.car_make}
            autoCapitalize="none"
        />
        <TextInput style={styles.Input} onChangeText={car_model => {
              this.setState({ car_model });
            }}
            placeholder="Car Model"
            value={this.state.car_model}
            autoCapitalize="none"
        />
        <TextInput style={styles.Input} onChangeText={car_license_plate => {
              this.setState({ car_license_plate });
            }}
            placeholder="Car License Plate"
            value={this.state.car_license_plate}
            autoCapitalize="none"
        />
        <Button text ="Start Trip" size="small" onPress = {this.startTrack} disabled = {this.state.disabled}/>
        <Button text ="Stop Trip" size="small" onPress = {this.stopTrack}/>
        <Button text ="Cancel Trip" size="small" onPress = {this.cancelTrack}/>
        <Text style ={[styles.trackTokenText]}>{ "Tracking Identifier : "}
            {this.state.tracking_identifier !== 0 ? this.state.tracking_identifier: null}
        </Text>
        <Text style ={[styles.trackTokenText]}>{ "Track Token : "}
            {this.state.track_token !== 0 ? this.state.track_token: null}
        </Text>
        <Text style ={[styles.siteIdText]}> { "Site ID : "}
        {this.state.site_id !== 0 ? this.state.site_id: null}
        </Text>
      </SafeAreaView>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 2,
    backgroundColor: '#fff',
    justifyContent: 'center',
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  trackTokenText: {
    color: '#FF00FF',
    textAlign: 'center',
    margin: 10
  },
  Input: {
    height: 40,
    borderColor: "blue",
    borderWidth: 1,
    padding: 8,
    margin: 4
  },
  siteIdText: {
    color: 'green',
    textAlign: 'center',
    margin: 10
  },
  listWrapper: {
    borderBottomColor: "#607D8B",
    borderBottomWidth: 1,
    borderTopColor: "#607D8B",
    borderTopWidth: 1,
    display: 'flex',
    marginBottom: 20
  },
  list: {
    padding: 10,
    textAlign: 'left'
  },
  listHighlighted: {
    backgroundColor: 'green'
  },
  listSeparator: {
    height: 1,
    width: "100%",
    backgroundColor: "#607D8B",
  }
});
