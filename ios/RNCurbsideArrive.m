#import "RNCurbsideArrive.h"
#import <React/RCTLog.h>
#import <React/RCTBridge.h>

@implementation RNCurbsideArrive
{
    CLLocationManager *locationManager;
    bool hasListeners;
}

RCT_EXPORT_MODULE(Curbside);

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"canNotifyMonitoringSessionUserAtSite", @"userApproachingSite", @"userArrivedAtSite",
             @"encounteredError", @"updatedTrackedSites", @"sessionChangedState"];
}
/**
 *  Will be called when this module's first listener is added.
 */
- (void)startObserving {
    hasListeners = YES;
    [CSUserSession currentSession].delegate = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        locationManager = [[CLLocationManager alloc] init];
        if ([locationManager respondsToSelector:@selector(requestAlwaysAuthorization)])
            [locationManager requestAlwaysAuthorization];
    });
}
/**
 *      Consumers should be using this method to create a new session with their usageToken. The state of the session
 *      can be checked using the sessionState property. The delegate is also notified of sessionState changes  by the
 *      delegate callback.
 *
 *      Note: The usage token is scoped to the Production and Sandbox. Make sure to use the production usage token when deploying
 *      your app to the Apple App Store.
 *
 */
RCT_EXPORT_METHOD(createSessionWithUsageToken:(NSString *)token)
{
    self.userSession = [CSUserSession createSessionWithUsageToken:token delegate: self];
    RCTLogInfo(@"Usage Token set to : %@",token);
}

/**
 *
 *      trackingIdentifier for the user who is logged into the device. This may be nil when the app is started, but as the
 *      user logs into the app, make sure this value is set. trackingIdentifier needs to be set to use session specific methods for starting
 *      trips or monitoring sites. This identifier will be persisted across application restarts.
 *
 *      When the user logs out, set this to nil, which will inturn end the user session or monitoring session.
 *
 *      @note The maximum length of the trackingIdentifier is 36 characters.
 *
 */
RCT_REMAP_METHOD(setTrackingIdentifier,setTrackingIdentifier:(NSString *)trackingIdentifier resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    self.userSession.trackingIdentifier = trackingIdentifier;
    RCTLogInfo(@"trackingIdentifier set to : %@",trackingIdentifier);
    resolve(trackingIdentifier);
}
/**
 *      Start a trip tracking the user to the site identified by the siteID. Call this method when
 *      the application thinks its appropriate to start tracking the user eg. Order is ready to be picked up at
 *      the site. This information is persisted across relaunch.
 *
 *      If an error occurs because of an invalid session state, permissions or authentication with the ARRIVE server,
 *      the CSUserSessionDelegate will be informed with the reason as to why startTripToSiteWithIdentifier: failed.
 */
RCT_EXPORT_METHOD(startTripToSiteWithIdentifier:(NSString *)siteID trackToken:(NSString *)trackToken)
{
    [self.userSession startTripToSiteWithIdentifier:siteID trackToken:trackToken];
    RCTLogInfo(@"start trip with the site Identifier :%@ & Track token : %@",siteID,trackToken);
}

/**
 *      Start a trip tracking the user to the site identified by the siteID for the user's intent of arrival at a given time or a time window.
 *      Call this method when the application thinks its appropriate to start tracking the user.
 *
 *      If the user's intent is to arrive at the site at a given time, then populate the fromDate with the arrival time. e.g. Pick up
 *      a order at 6.00pm. Set the fromDate to be 6.00pm
 *
 *      If the user's intent is to arrive at the site at a given time window, then populate the fromDate and toDate with the time window.
 *
 *      If an error occurs because of an invalid session state, permissions or authentication with the ARRIVE server,
 *      the CSUserSessionDelegate will be informed with the reason as to why startTripToSiteWithIdentifierWithPickupWindow: failed.
 */
RCT_EXPORT_METHOD(startTripToSiteWithIdentifierWithPickupWindow:(NSString *)siteID trackToken:(NSString *)trackToken etaFromDate:(NSDate *)fromDate toDate:(nullable NSDate *)toDate)
{
    [self.userSession startTripToSiteWithIdentifier:siteID trackToken:trackToken etaFromDate:fromDate toDate: toDate];
    RCTLogInfo(@"start trip with the site Identifier : %@ & Track token : %@ & etaFromDate : %@ & toDate : %@",siteID,trackToken,fromDate,toDate);
}

/**
 *      Completes the trip for the user to the site identified by the siteID with the given trackToken.
 *       If no trackToken is specified, then *all*   trips to this site  will be completed.
 *
 *      @note Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
 */
RCT_EXPORT_METHOD(completeTripToSiteWithIdentifier:(NSString *)siteID trackToken:(nullable NSString *)trackToken)
{
    [self.userSession completeTripToSiteWithIdentifier:siteID trackToken:trackToken];
    RCTLogInfo(@"Complete trip with the site Identifier :%@ & Track token : %@",siteID,trackToken);
}

/**
 *      Cancels the trip for the user to the given site identified by the siteID with the given trackToken.
 *      If no trackToken is set, then *all* trips to this site are cancelled.
 *
 *      @note Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
 */
RCT_EXPORT_METHOD(cancelTripToSiteWithIdentifier:(NSString *)siteID trackToken:(nullable NSString *)trackToken)
{
    [self.userSession cancelTripToSiteWithIdentifier:siteID trackToken:trackToken];
    RCTLogInfo(@"Cancel trip with the site Identifier :%@ & Track token : %@",siteID,trackToken);
}

/**
 *      This method would complete all trips for this user across all devices.
 *
 *      @note Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
 */
RCT_EXPORT_METHOD(completeAllTrips)
{
    [self.userSession completeAllTrips];
}

/**
 *      This method will cancels all trips for all sites for the user.
 *
 *      @note Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
 */
RCT_EXPORT_METHOD(cancelAllTrips)
{
    [self.userSession cancelAllTrips];
}

/**
 *      This method would update the ARRIVE SDK with the current state of tracked trips for the user from the server.
 */
RCT_EXPORT_METHOD(updateTripsFromServer)
{
    [self.userSession updateTripsFromServer];
}

/**
 *      This method will calculate the estimated time of arrival to a site. Negative value means ETA is unknown.
 *      Unit : seconds
 */
RCT_EXPORT_METHOD(etaToSiteWithIdentifier:(nonnull NSString *)siteID
                  fromLocation:(nonnull CLLocation *)fromLocation
                  transportationMode:(CSTransportationMode)transportationMode
                  completionHandler:(nonnull void (^)(int))completionHandler)
{
    [self.userSession etaToSiteWithIdentifier:siteID fromLocation:fromLocation transportationMode:transportationMode completionHandler:completionHandler];
}

/**
 *      This method simulates a mock trip to the site. It does the following :
 *          - startTripToSiteWithIdentifier: with the given site
 *          - Gets a route from the given startLocation to the site
 *          - Sends up the location on the route to the site at regular intervals to ARRIVE Server
 *
 *      @note You must cancel the trip using cancelMockTrip, once you are done. You can only do one Mock trip at a time.
 */
RCT_EXPORT_METHOD(startMockTripToSiteWithIdentifier:(nonnull NSString *)siteID
                  fromLocation:(nonnull CLLocation *)startLocation)
{
    [self.userSession startMockTripToSiteWithIdentifier:siteID fromLocation:startLocation];
}

/**
 *      This method cancels the mock trip started by the startMockTripToSite:fromLocation: method
 *
 *      @note You must call this method if you have started a MockTrip using startMockTripToSiteWithIdentifier:fromLocation:
 */
RCT_EXPORT_METHOD(cancelMockTrip)
{
    [self.userSession cancelMockTrip];
}
/**
 *      Contact info - fullName, email address, sms number, Vehicle info - Make, Model & License Plate of the user.
 */
RCT_EXPORT_METHOD(setUserInfo:(nullable NSString *)fullName email:(nullable NSString *)emailAddress smsNumber:(nullable NSString *)smsNumber
                  vehicleMake:(nullable NSString *)vehicleMake vehicleModel:(nullable NSString *) vehicleModel vehicleLicensePlate:(nullable NSString *)vehicleLicensePlate)
{
    CSUserInfo *userInfo = [[CSUserInfo alloc] init];
    userInfo.fullName = fullName;
    userInfo.emailAddress = emailAddress;
    userInfo.smsNumber = smsNumber;
    userInfo.vehicleMake = vehicleMake;
    userInfo.vehicleModel = vehicleModel;
    userInfo.vehicleLicensePlate = vehicleLicensePlate;
    self.userSession.userInfo = userInfo;
    RCTLogInfo(@"setUserInfo : %@",userInfo);
    
}

// Call backs

- (void)session:(CSUserSession *)session canNotifyMonitoringSessionUserAtSite:(CSSite *)site {
    if (hasListeners) {
        [self sendEventWithName:@"canNotifyMonitoringSessionUserAtSite" body:[self siteEncode:site]];
    }
}

- (void)session:(CSUserSession *)session userApproachingSite:(CSSite *)site {
    if (hasListeners) {
        [self sendEventWithName:@"userApproachingSite" body:[self siteEncode:site]];
    }
}

- (void)session:(CSUserSession *)session userArrivedAtSite:(CSSite *)site {
    if (hasListeners) {
        [self sendEventWithName:@"userArrivedAtSite" body:[self siteEncode:site]];
    }
}

- (void)session:(CSUserSession *)session encounteredError:(NSError *)error forOperation:(CSUserSessionAction)customerSessionAction {
    if (hasListeners) {
        [self sendEventWithName:@"encounteredError" body:[error localizedDescription]];
    }
}

- (void)session:(CSUserSession *)session updatedTrackedSites:(NSSet<CSSite *> *)trackedSites {
    if (hasListeners) {
        [self sendEventWithName:@"updatedTrackedSites" body:[self sitesEncode:trackedSites]];
    }
}

- (void)session:(CSSession *)session changedState:(CSSessionState)newState {
    if (hasListeners) {
        [self sendEventWithName:@"sessionChangedState" body:[self sessionStateEncode:newState]];
    }
}
// Helper methods
- (NSDictionary*)siteEncode:(CSSite *)site {
    NSMutableDictionary *encodedSite = [[NSMutableDictionary alloc] init];
    [encodedSite setValue:site.siteIdentifier forKey:@"siteIdentifier"];
    [encodedSite setValue:[NSNumber numberWithInt:site.distanceFromSite] forKey:@"distanceFromSite"];
    [encodedSite setValue:[self userStatusEncode:site.userStatus] forKey:@"userStatus"];
    [encodedSite setValue:[self tripsEncode:site.tripInfos] forKey:@"trips"];
    return encodedSite;
}
- (NSArray*)sitesEncode:(NSSet<CSSite *> *)sites {
    NSMutableArray *result = [[NSMutableArray alloc] init];
    NSEnumerator<CSSite*> *enumerator = [sites objectEnumerator];
    CSSite *site;
    while (site = [enumerator nextObject])
    {
        [result addObject:[self siteEncode:site]];
    }
    return result;
}

- (NSString*)userStatusEncode:(CSUserStatus)status {
    switch(status) {
        case CSUserStatusArrived:
            return @"arrived";
            break;
        case CSUserStatusInTransit:
            return @"inTransit";
            break;
        case CSUserStatusApproaching:
            return @"approaching";
            break;
        case CSUserStatusUserInitiatedArrived:
            return @"userInitiatedArrived";
            break;
        default:
            return @"unknown";
    }
}
- (NSArray*)tripsEncode:(NSArray<CSTripInfo *> *)trips {
    NSMutableArray *result = [[NSMutableArray alloc] init];
    NSEnumerator<CSTripInfo*> *enumerator = [trips objectEnumerator];
    CSTripInfo *trip;
    while (trip = [enumerator nextObject]) {
        [result addObject:[self tripEncode:trip]];
    }
    return result;
}
- (NSDictionary*)tripEncode:(CSTripInfo *)trip {
    NSMutableDictionary *encodedTrip = [[NSMutableDictionary alloc] init];
    [encodedTrip setValue:trip.trackToken forKey:@"trackToken"];
    [encodedTrip setValue:trip.startDate forKey:@"startDate"];
    [encodedTrip setValue:trip.destID forKey:@"destID"];
    return encodedTrip;
}
- (NSString*)sessionStateEncode:(CSSessionState)state {
    switch(state) {
        case CSSessionStateUsageTokenNotSet:
            return @"usageTokenNotSet";
            break;
        case CSSessionStateInvalidKeys:
            return @"invalidKeys";
            break;
        case CSSessionStateAuthenticated:
            return @"authenticated";
            break;
        case CSSessionStateValid:
            return @"valid";
            break;
        case CSSessionStateNetworkError:
            return @"networkError";
            break;
        default:
            return @"unknown";
    }
}
@end
