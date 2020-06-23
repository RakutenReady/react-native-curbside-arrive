#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#elif __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import "React/RCTBridgeModule.h"   // Required when used as a Pod in a Swift project
#endif
#import <React/RCTEventEmitter.h>
#import <Curbside/Curbside.h>

@interface CurbsideArrive : RCTEventEmitter <RCTBridgeModule,CSUserSessionDelegate>

/**
 *      This class is the heart of the transmit application. Once the session is created with the usageToken
 *      you can identify the user to the session by setting the trackingIdentifier and then be able to start/complete/cancel trips to different
 *      sites which are predefined on the ARRIVE platform.
 *
 */

@property (nonatomic, strong,nonnull) CSUserSession *userSession;

/**
 *      Contact info - fullName, email address, sms number, Vehicle info - Make, Model & License Plate of the user.
 */

- (void)setUserInfo:(nullable NSString *)fullName email:(nullable NSString *)emailAddress smsNumber:(nullable NSString *)smsNumber
        vehicleMake:(nullable NSString *)vehicleMake vehicleModel:(nullable NSString *) vehicleModel vehicleLicensePlate:(nullable NSString *)vehicleLicensePlate ;
@end
