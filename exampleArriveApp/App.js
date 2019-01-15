/**
 * PieTracker React Native App with Curbside SDK
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {FlatList,Platform, StyleSheet, Text, SafeAreaView, View, TextInput, TouchableHighlight} from 'react-native';
import { PermissionsAndroid } from 'react-native';
import Button from './src/components/Button';
import FetchLocation from './src/components/FetchLocation';


import { NativeModules } from 'react-native';
const curbside = NativeModules.Curbside;
const USAGE_TOKEN = '5456fd004166c9226ec3f2cb1ec567f0556af771305cc3e38fb0c3419833f48b'; // Piecompany

const instructions = Platform.select({
  ios: 
    'PieTracker React Native Arrive App,\n' + 
    'Press Cmd+R to reload,\n' + 
    'Cmd+D or shake for dev menu',
  android:
    'PieTracker React Native Arrive App,\n' +
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

export async function requestLocationPermission() 
{
  try {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      {
        'title': 'PieTracker React Native App',
        'message': 'PieTracker React Native App access to your location '
      }
    )
    if (granted === PermissionsAndroid.RESULTS.GRANTED) {
      console.log("You can use the location")
    } else {
      console.log("location permission denied")
    }
  } catch (err) {
    console.warn(err)
  }
}

export default class App extends Component {
  constructor(props) {
    super(props)
    curbside.createSessionWithUsageToken(USAGE_TOKEN)
    this.state = { 
      user_location: null, 
      data_source: []
    }
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
  async componentWillMount() {
    if(Platform.OS === 'android') {
      await requestLocationPermission()
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
  getSiteLocationHandler = () => {
    navigator.geolocation.getCurrentPosition(
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

  render() {
    
    if(this.state.track_token && this.state.disabled === true && this.state.tracking_identifier) {
      curbside.setTrackingIdentifier(this.state.tracking_identifier)
      curbside.startTripToSiteWithIdentifier(this.state.site_id,this.state.track_token)
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
        <Text style={styles.instructions}>{instructions}</Text>
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
