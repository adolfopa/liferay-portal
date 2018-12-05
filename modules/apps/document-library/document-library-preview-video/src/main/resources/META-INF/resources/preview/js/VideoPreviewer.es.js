import Component from 'metal-component';
import Soy from 'metal-soy';

import templates from './VideoPreviewer.soy';

/**
 * Component that create an video player
 * @review
 */
class VideoPreviewer extends Component {
}

/**
 * State definition.
 * @review
 * @static
 * @type {!Object}
 */
VideoPreviewer.STATE = {
};

Soy.register(VideoPreviewer, templates);
export {VideoPreviewer};
export default VideoPreviewer;