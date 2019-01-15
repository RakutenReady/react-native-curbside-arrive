import React from 'react';
import Button from './Button';

const fetchLocation = props => {
    return (
        <Button text ="Get nearby sites" size="small" onPress = {props.onGetLocation}/>
    );
}

export default fetchLocation; 